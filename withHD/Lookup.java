import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Lookup 
{
	private MRUCaching mru;
	ArrayList<Integer>  neighborPort;
	LookupObject l;

	public Lookup( MRUCaching mru, ArrayList<Integer>  neighborPort, LookupObject l)
	{
		this.mru = mru;
		this.neighborPort = neighborPort;
		this.l = l;
	}

	public void LookupWithCoordinates(String input)
	{
		if(mru.containsCoordinates(input))
		{
			Node node = mru.get(input);
			System.out.println("\nMATCH FOUND LOCALLY:" + node); //Observed at robot
			l.hit = l.hit+1;	
		}
		else 
		{
			l.miss = l.miss +1;
			if (mru.containsCoordinatesinHD(input))


			{
				Node node = mru.getfromHD(input);
				System.out.println("\nMATCH FOUND in LOCAL HARDDISK:" + node); //Observed at robot
				//remove from HD
				mru.hardDisk.remove(input);
				//add to cache
				mru.cache.put(input, node);
			}
			else
			{
				System.out.println("\nNot at this robot, checking at neighbours");
				int i;
				for(i = 0; i < neighborPort.size(); i++)
				{
					// send request to neighbour
					Socket s;
					try 
					{
						s = new Socket("localhost", neighborPort.get(i));
						ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
						oos.writeInt(1);
						oos.flush();

						oos.writeObject(input);
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

						if(received.tempValue != -1)
						{
							mru.insert(input, received);
							System.out.println(received);
							break;
						}

					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					} 
					catch (ClassNotFoundException e) 
					{
						e.printStackTrace();
					}
				}
				if(i==neighborPort.size())
				{
					System.out.println("Data not found on any neighbor!");
				}
			}
		}
	}

	/*public void LookupWithTemperature(int input)
	{
		for (String node : mru.hash.keySet()) 
		{
			if(mru.getDataAtCoordinates(node).tempValue == input)
			{
				System.out.println("\nObserved at robot:"+mru.getDataAtCoordinates(node));
				hit++;
				System.out.println("Cache Hit Rate: "+cacheHitRate()+"%\n");
				return;
			}		

		}
		for (String node : sharedmru.hash.keySet()) 
		{
			if(sharedmru.getDataAtCoordinates(node).tempValue == input)
			{
				System.out.println("\nShared with robot:"+sharedmru.getDataAtCoordinates(node));
				hit++;
				System.out.println("Cache Hit Rate: "+cacheHitRate()+"%\n");
				return;
			}
		}
		System.out.println("\nNot at this robot, checking at neighbours");
		int i;
		for(i = 0; i < neighborPort.size(); i++)
		{
			// send request to neighbour
			Socket s;
			try 
			{
				s = new Socket("localhost", neighborPort.get(i));
				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				oos.writeInt(2);
				oos.flush();

				oos.writeInt(input);
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

				if(received.tempValue != -1)
				{
					System.out.println(received);
					return;
				}

			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			} 
			catch (ClassNotFoundException e) 
			{
				e.printStackTrace();
			}
		}
		System.out.println("Data not found on any neighbor!");
	}*/

	public  int cacheHitRate()
	{
		return  (int) (( (float)l.hit / (float)(l.hit+l.miss) ) *100);
	}

	public void getInfo()
	{
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
		int inputType;

		System.out.println("Enter input choice: \n1. Coordinates \n2. Temperature \n3. Exit");
		inputType = sc.nextInt();
		String input ="";
		switch(inputType)
		{	
		case 1:
			System.out.println("Enter Coordinates:");
			int x = sc.nextInt();
			int y = sc.nextInt();
			input = x+" "+y; 
			LookupWithCoordinates(input);
			System.out.println(l.hit+"  "+l.miss);
			System.out.println("Cache Hit Rate: " + cacheHitRate() + "%\n");
			break;
			/*case 2:
			System.out.println("Enter Temperature:");
			int temp = sc.nextInt(); 
			access_count++;							
			LookupWithTemperature(temp);			
			break;*/
		case 3:
			System.out.println("Exiting from lookup");
			break;
		default:
			System.out.println("Invalid input");
			break;
		}
	}
}