import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Writer implements Runnable 
{
	private Socket towrite;
	private MRUCaching mru;
	MRUCaching sharedmru;
	//coorinates string
	//temp int
	
	
	public Writer(Socket clientSocket, MRUCaching mru, MRUCaching sharedmru) 
	{
		this.towrite = clientSocket;
		this.mru = mru;
		this.sharedmru = sharedmru;
	}

	@Override
	public void run() 
	{
		try 
		{
			ObjectOutputStream oos = new ObjectOutputStream(towrite.getOutputStream());
			oos.flush();
			
			ObjectInputStream ois = new ObjectInputStream(towrite.getInputStream());
			
			int choice = -1;
			while(true)
			{
				choice = ois.readInt();
				if(choice != -1)
				{
					break;
				}
			}
			
			switch(choice)
			{
			case 0:
				//shares most popular/frequent from all recent node
				oos.writeObject(mru.getMostPopular());
				break;
			case 1:
				// look up case 1
				String input;
				while(true)
				{
					input = (String) ois.readObject();
					if(input != null)
					{
						break;
					}
				}
				Data toReturn = null;
				if(mru.containsCoordinates(input))
				{
					toReturn = mru.get(input).data;
				}
				else if(sharedmru.containsCoordinates(input))
				{
					toReturn = sharedmru.get(input).data;
				}
				else
				{
					toReturn = new Data(); 
				}
				oos.writeObject(toReturn);
				break;
			case 2:
				// look up case 2
				int temp = -1;
				while(true)
				{
					temp = ois.readInt();
					if(temp != -1)
					{
						break;
					}
				}
				
				Data data = mru.getDataAtTemperature(temp);
				if(data.tempValue == -1)
				{
					data = sharedmru.getDataAtTemperature(temp);
				}
				oos.writeObject(data);
				break;
			}
			// close connection
			ois.close();
			oos.close();
			towrite.close();
		} 
		catch (IOException e) 
		{
			System.out.println("Connection to " + towrite.getPort() + " lost");
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
	}
}
