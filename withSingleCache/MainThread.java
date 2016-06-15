import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
//import javax.swing.JOptionPane;

public class MainThread
{

	final static int TEMPTHRESHOLD = 170;
	final static int CAPACITY = 10; //caching capacity of each robot
	final static int SHARING_DISTANCE = 25; //SQRT(25) = 5
	final static int CACHING_DELAY = 10;

	static MRUCaching mru;


	public static void run( FileReader robotfile, PrintStream output, String name)
	{	

		ArrayList<Integer> neighborPort = new ArrayList<>();
		ArrayList<Integer> failedNeighborPort = new ArrayList<>();
		int myport;
		Scanner sr = new Scanner(robotfile);		
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);

		System.out.println("Running... "+name);	

		//System.out.println("Enter capacity of each robot");
		//int capacity = sc.nextInt();

		MRUCaching mru = new MRUCaching(CAPACITY);

		myport = sr.nextInt();

		// send's connection request
		Server server = new Server(myport, mru);
		Thread starter = new Thread(server);
		starter.start();

		while(sr.hasNext())
		{
			neighborPort.add(sr.nextInt());
		}
		sr.close();

		try 
		{
			robotfile.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}			

		System.out.println("Press any key to start caching // ON button");

		@SuppressWarnings("unused")
		String input = sc.next();

		LookupRequestThread lookuprequest = new LookupRequestThread(mru,neighborPort);
		Thread thread = new Thread(lookuprequest);

		thread.start();

		while(true)  
		{
			Data data = new Data(name);
			//data as value as it receives both coordinate and temp from others
			mru.insert(data.getKey(), data);  

			if(neighborPort.size() > 0)
			{
				// select one neighbour
				Random random = new Random();
				int neigbour = random.nextInt(neighborPort.size());
				int neighbour_port = neighborPort.get(neigbour);
				
				
				try 
				{
					// send request to neighbour
					Socket s = new Socket("localhost", neighbour_port);

					ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
					oos.writeInt(0);
					oos.flush();
		
					ObjectInputStream ois = new ObjectInputStream(s.getInputStream());

					Data received = null;
					
					// get data from neighbor
					while(received == null)
					{
						received = (Data) ois.readObject();
					}
					
					// close connection
					oos.close();
					ois.close();
					s.close();

					if(received.tempValue == -1)
					{
						//Neighbour sends empty node
						System.out.println("Neighbour sent empty node");
					}
					else
					{
						// calculate distance here
						int distance = (int) ( Math.pow( (received.getX() - data.getX()) , 2) + Math.pow( (received.getY() - data.getY()) , 2) ) ; 

						String location = received.x + " " + received.y; 
						
						//arbitrary threshold
						if(received.tempValue >= TEMPTHRESHOLD) 
						{
							System.out.println("High Temperature Alert at " + location);
							//JOptionPane.showMessageDialog(null, "High Temperature Alert!!");
						}
						else
						{
							double tempRisePerMinute = 0;
							
							if(mru.containsCoordinates(location))
							{
								Data oldValues = mru.getDataAtCoordinates(location);
								if(received.time - oldValues.time > 0)
								{
									tempRisePerMinute =  (received.tempValue - oldValues.tempValue) / (received.time - oldValues.time);
								}
							}
						
							if(mru.containsCoordinates(location))
							{
								Data oldValues = mru.getDataAtCoordinates(location);
								if(received.time - oldValues.time > 0)
								{
									double tempRisePerMinute1 =  (received.tempValue - oldValues.tempValue) / (received.time - oldValues.time);
									if(tempRisePerMinute1 > tempRisePerMinute)
									{
										tempRisePerMinute = tempRisePerMinute1;
									}
								}
							}
							
							if(tempRisePerMinute > 0.2) //means 12 degree in 60 min  
							{
								System.out.println("WARNING: Temperature rising rapidly at " + location+" under coverage area of "+name);
							}
						}
						
						//share data with robots with 5 unit distance
//						if(distance >= SHARING_DISTANCE)  // sqrt(25) = 5 
//						{
							mru.insert(received.getKey(), received);
//						}

						//arbitrary threshold
						if(received.tempValue >= TEMPTHRESHOLD) 
						{
							System.out.println("FIRE ALERT: High Temperature at " + location);
							//JOptionPane.showMessageDialog(null, "High Temperature Alert!!");
						}
						
						System.out.println("\n"+name+" received data from " + received.name +"\nData:  Temperature: (" + received.getTemperature() +") observed at Coordinates: ("+received.getKey()+ ")");
					}
				} 
				catch (IOException e) 
				{
					//neighbor failed to send
					System.out.print("WARNING: Robot at port "+ neighbour_port +" failed!");
					//print neighbor failed
					neighborPort.remove(neighborPort.indexOf(neighbour_port));
					failedNeighborPort.add(neighbour_port);
				}
				catch (ClassNotFoundException e) 
				{
					e.printStackTrace();
				}
			}

			//printing to output file
			System.out.println("\n==================== ROBOT's CACHE ====================");
			System.out.println("Local MRU");
			for (String node : mru.hash.keySet()) 
			{
				int temperature = mru.getDataAtCoordinates(node).tempValue;
				int frequency = mru.getDataAtCoordinates(node).frequency;
				System.out.println("(x, y, temperature,frequency) : " + node + " " + temperature + " " + frequency);
				output.println("Local (x, y, temperature,frequency) : " + node + " " + temperature + " " + frequency);
			}

//			System.out.println("Shared MRU");
//			for (String node : sharedmru.hash.keySet()) 
//			{
//				int temperature = sharedmru.getDataAtCoordinates(node).tempValue;
//				int frequency = sharedmru.getDataAtCoordinates(node).frequency;
//				System.out.println("(x, y, temperature,frequency) : " + node + " " + temperature + " " + frequency);
//				output.println("Shared (x, y, temperature,frequency) : " + node + " " + temperature + " " + frequency);
//			}	
			System.out.println("=======================================================\n");

			//5 sec delay in inputs for noticeable changes
			//optional
			try 
			{
				Thread.sleep(10000);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			} 

			//try to make connection to failed neighbors
			Iterator<Integer> i = failedNeighborPort.iterator();
			while (i.hasNext()) 
			{
				Integer failedPort = i.next(); // must be called before you can call i.remove()
				// send request to neighbor
				Socket s;
				try {
					s = new Socket("localhost", failedPort);
					ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
					oos.writeInt(0);
					oos.flush();
					
					ObjectInputStream ois = new ObjectInputStream(s.getInputStream());

					
					Data received = null;

					// get data from neighbor
					while(received == null)
					{
						received = (Data) ois.readObject();
					}

					// close connection
					oos.close();
					ois.close();
					s.close();

					// connection was successful
					// add back to neighbors
					neighborPort.add(failedPort);
					i.remove();

				} 
				catch (IOException e) 
				{
					// do nothing
				}
				catch (ClassNotFoundException e) 
				{
					e.printStackTrace();
				}
			}
			data = new Data(name);	
		} 
	}

	public static void main(String args[]) 
	{
		//remove for loop while running on consoles
		System.out.println("\nPRESS 'l' or 'L' ANYTIME FOR LOOKUP OR CACHING WILL CONTINUE!");
		try
		{
			FileReader robotfile = new FileReader("/Users/ruturaj/Desktop/capstone/TestData/4robots/robot"+ args[0]  +".txt"); //i+1 when using loop
			PrintStream outfile = new PrintStream(new File("/Users/ruturaj/Desktop/capstone/TestData/4robots/output"+ args[0] +".txt"));
			run(robotfile,outfile,"Robot "+ args[0]);
		} 
		catch (FileNotFoundException e ) 
		{
			e.printStackTrace();
		}
	}
}
