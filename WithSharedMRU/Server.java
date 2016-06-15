import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable 
{
	private int port;
	private MRUCaching mru;
	MRUCaching sharedmru;

	public Server(int port, MRUCaching mruCaching, MRUCaching sharedmru) 
	{
		this.port = port;
		this.mru = mruCaching;
		this.sharedmru = sharedmru;
	}

	@Override
	public void run()
	{
		while(true)
		{
			ServerSocket connection;
			try {
				connection = new ServerSocket(port);
				while (true) 
				{
					//System.out.println("Waiting for connection request");
					Socket newConnection;
					try {
						newConnection = connection.accept();
						//System.out.println("Connected to " + newConnection.getPort());
						Writer writer = new Writer(newConnection, mru,sharedmru);  //coordinates //temp
						Thread name = new Thread(writer);
						name.start();			
					} 
					catch (IOException e) 
					{
						System.out.println("Can not accept connection");
					}	
				}
			} 
			catch (IOException e) 
			{
				System.out.println("Can not create server socket");
			}
		}
	}

}
