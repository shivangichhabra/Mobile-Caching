import java.util.ArrayList;
import java.util.Scanner;

public class LookupRequestThread extends Thread 
{
	
	MRUCaching mru;
	ArrayList<Integer> neighborPort;
	
	public LookupRequestThread(MRUCaching mru, ArrayList<Integer>  neighborPort) {
		this.mru = mru;
		this.neighborPort = neighborPort;
	}

	public void run() 
	{
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
		String in;
		while(true)
		{
			in = sc.nextLine();
			if(in.equals("L") || in.equals("l"))
			{
				//do lookup  
				Lookup l = new Lookup(mru, neighborPort);
				
				long startTime = System.currentTimeMillis();
				l.getInfo();
				long endTime = System.currentTimeMillis();
				
				System.out.println("\nResponse Time: "+(endTime - startTime)+" milliseconds");
			}
		}
	}
}
