import java.io.Serializable;
import java.util.Random;

public class Data implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final int MAX_X = 10;
	private static final int MAX_Y = 10;
	int x, y;
	int tempValue;
	String name;
	int time; //timestamp for history lookup
	int frequency;
	
	// constructor for empty data
	Data()
	{
		this.name = "empty";
		x = 0;
		y = 0;
		tempValue = -1;	
		frequency = 0;
	}
	
	Data(String name)
	{
		this.name = name;
		Random random = new Random(); 
		x = random.nextInt(MAX_X);
		y = random.nextInt(MAX_Y);
		tempValue = random.nextInt(200);
		time = (int)(System.currentTimeMillis() / 60000);
		frequency = 0;
	}

	public String getKey()
	{
		return x+" "+y;
	}

	public int getTemperature()	
	{
		return tempValue;
	}
	
	public long getTime()
	{
		return time;
	}

	public int getX()
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}

	@Override
	public String toString() 
	{
		return "MATCH FOUND AT " + name+"\nINFO: Coordinates(x,y) = (" + x+","+y  + ") / Temperature(F) = " + getTemperature();
	}
}
