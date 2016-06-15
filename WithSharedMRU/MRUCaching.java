import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MRUCaching 
{
	private Node head;
	private Node tail;
	private int capacity;
	HashMap<String, Node> hash;

	/*
	 * Constructor
	 */
	public MRUCaching(int capacity) 
	{
		hash = new HashMap<String, Node>();
		this.capacity = capacity;
	}

	/*
	 * Inserts a new node
	 */
	public void insert(String key, Data data) 
	{
		if (hash.containsKey(key)) 
		{
			Node node = hash.get(key);
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
		if (hash.size() >= capacity) 
		{ 
			remove();
		}

		// New slot: space available in cache for new value
		Node node = new Node(data);
		add(node);
		hash.put(key, node);
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
			while(node.next != null){
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
			hash.remove(head.data.getKey());
			return;
		}

		// Remove from end
		Node last_element = tail;
		tail = last_element.prev;
		last_element.prev.next = null;
		hash.remove(last_element.data.getKey());

	}

	/*
	 * returns value at given key
	 */
	public Node get(String key)
	{
		Node toReturn =  hash.get(key);
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
		
		for (String n : hash.keySet()) 
		{
			Node current = hash.get(n);

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
		Node info = hash.get(input);
		return info.data;
	}

	/*
	 * checks if coordinates exists
	 */
	public boolean containsCoordinates(String input)
	{
		if(hash.containsKey(input))
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

		for (String n : hash.keySet()) 
		{
			if(hash.get(n).data.tempValue == input)
			{
				info = hash.get(n);	
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
