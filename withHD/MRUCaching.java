import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MRUCaching 
{
	private Node head;
	private Node tail;
	private int capacity;
	HashMap<String, Node> cache;
	HashMap<String, Node> hardDisk;
	/*
	 * Constructor
	 */
	public MRUCaching(int capacity) 
	{
		cache = new HashMap<String, Node>();
		hardDisk = new HashMap<String, Node>();
		this.capacity = capacity;
	}

	/*
	 * Inserts a new node
	 */
	public void insert(String key, Data data) 
	{
		if (cache.containsKey(key)) 
		{
			Node node = cache.get(key);
			int frequency = node.data.frequency;
			if(data.frequency > frequency)
			{
				frequency = data.frequency;
			}
			node.data = data;
			node.data.frequency = frequency;
			//update position
			update(node);
			return;
		}

		// Out of capacity, cleaning the oldest slot (last element)
		if (cache.size() >= capacity) 
		{ 
			remove();
		}

		// New slot: space available in cache for new value
		Node node = new Node(data);
		add(node);
		cache.put(key, node);
	}

	/*
	 * Updates location of existing node
	 */
	public void update(Node node) 
	{

		if (node.prev != null) 
		{
			// remove from present location
			node.prev.next = node.next;
			if (node.next != null)
			{
				node.next.prev = node.prev;
			}
			else
			{
				tail = node.prev;
			}

			// add to the front
			node.next = head;
			node.prev = null;

			if(head != null)
			{
				head.prev = node;
			}

			head = node;

			if(tail == null)
			{
				tail = head;
			}
		}
	}

	/*
	 * Adds MRU to the front of queue
	 */
	private void add(Node node) 
	{
		node.next = null;
		node.prev = null;

		// First element
		if (head == null) 
		{
			head = node;
			tail = node;
			//return;
		}
		else
		{
			// Existing element
			head.prev = node;
			node.next = head;
			head = node;
			while(node.next != null)
			{
				node = node.next;
			}
			tail = node;
		}

	}

	/*
	 * Removes LRU from end
	 */
	private void remove() 
	{		
		if (tail == null) 
		{
			return;
		}

		if(head == tail)
		{
			head = null;
			tail = null;
			cache.remove(head.data.getKey());
			return;
		}

		// Remove from end
		Node last_element = tail;
		tail = last_element.prev;
		last_element.prev.next = null;
		cache.remove(last_element.data.getKey());
		if(hardDisk.containsKey(last_element.data.getKey()))
		{
			hardDisk.get(last_element.data.getKey()).data = last_element.data;
		}
		else
		{
			hardDisk.put(last_element.data.getKey(), last_element);
		}
	}

	/*
	 * returns value at given key
	 */
	public Node get(String key)
	{
		Node toReturn =  cache.get(key);
		toReturn.data.frequency++;
		update(toReturn);
		return toReturn;  //returns node i.e value in hashMap
	}
	
	/*
	 * returns value at given key
	 */
	public Node getfromHD(String key)
	{
		Node toReturn =  hardDisk.get(key);
		toReturn.data.frequency++;
		update(toReturn);
		return toReturn;  //returns node i.e value in hashMap
	}

	/*
	 * returns most popular node
	 * if more than one most popular nodes
	 * then return one at random
	 * for sharing 
	 */
	public Data getMostPopular() 
	{
		int mostpopularFrequency = -1;
		ArrayList<Node> all_populars = new ArrayList<>();
		Node mostpopular = null;
		
		for (String n : cache.keySet()) 
		{
			Node current = cache.get(n);

			if(mostpopularFrequency < current.data.frequency)
			{
				all_populars.clear();
				mostpopularFrequency = current.data.frequency;
				all_populars.add(current);
			}
			else if(mostpopularFrequency == current.data.frequency)
			{
				all_populars.add(current);
			}
		}

		if(all_populars.size() == 0)
		{
			// create empty data
			Data data = new Data();
			mostpopular = new Node(data);
		}
		else
		{
			Random r = new Random();
			mostpopular = all_populars.get(r.nextInt(all_populars.size()));
		}
		return mostpopular.data;
	}
	
	/*
	 * lookup with coordinates
	 */
	public Data getDataAtCoordinates(String input)
	{
		Node info = cache.get(input);
		return info.data;
	}

	/*
	 * lookup with coordinates
	 */
	public Data getDataAtCoordinatesHD(String input)
	{
		Node info = hardDisk.get(input);
		return info.data;
	}
	
	/*
	 * checks if coordinates exists
	 */
	public boolean containsCoordinates(String input)
	{
		if(cache.containsKey(input))
		{
			return true;
		}
		return false;
	}	
	
	/*
	 * checks if coordinates exists
	 */
	public boolean containsCoordinatesinHD(String input)
	{
		if(hardDisk.containsKey(input))
		{
			return true;
		}
		return false;
	}

	/*
	 * lookup with temperature
	 */
	public Data getDataAtTemperature(int input)
	{	
		Node info = null;

		for (String n : cache.keySet()) 
		{
			if(cache.get(n).data.tempValue == input)
			{
				info = cache.get(n);	
			}
		}

		if(info == null)
		{
			System.out.println("Searching on next neighbor");
			return new Data();
		}

		return info.data;
	}

}
