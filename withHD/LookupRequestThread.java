import java.util.ArrayList;
import java.util.Scanner;

public class LookupRequestThread extends Thread 
{
	
	MRUCaching mru;
	ArrayList<Integer> neighborPort;
	LookupObject lo ;
	
	public LookupRequestThread(MRUCaching mru, ArrayList<Integer>  neighborPort) {
		this.mru = mru;
		this.neighborPort = neighborPort;
		this.lo = new LookupObject() ;
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
				Lookup l = new Lookup(mru, neighborPort, lo);
				
				long startTime = System.currentTimeMillis();
				l.getInfo();
				long endTime = System.currentTimeMillis();
				
				System.out.println("\nResponse Time: "+(endTime - startTime)+" milliseconds");
			}
		}
	}
}
