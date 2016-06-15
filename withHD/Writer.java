import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Writer implements Runnable 
{
	private Socket towrite;
	private MRUCaching mru;
	
	
	public Writer(Socket clientSocket, MRUCaching mru) 
	{
		this.towrite = clientSocket;
		this.mru = mru;
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
				else if (mru.containsCoordinatesinHD(input))
				{
					toReturn = mru.getfromHD(input).data;
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
