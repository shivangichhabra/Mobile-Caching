public class Node
{
	Node next;
	Node prev;
	Data data;

	public Node(Data data) 
	{
		this.data = data;
	}

	@Override
	public String toString() 
	{
		return "\nINFO: Coordinates(x,y) = (" + data.getKey()  + ") / Temperature(F) = " + data.getTemperature();
	}
}
