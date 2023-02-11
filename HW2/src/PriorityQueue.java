// kn9558
// ai6358

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityQueue {
	// helper class used to represent nodes in the priority queue
	class Node {
		private String name;
		private int priority;
		private Node next;
		private ReentrantLock lock;
		
		public Node(String name, int priority) {
			this.name = name;
			this.priority = priority;
			this.next = null;
			this.lock = new ReentrantLock();
		}
		
		public String getName() {
			return this.name;
		}
		
		// getters and setters
		public int getPriority() {
			return this.priority;
		}
		
		public Node getNext() {
			return this.next;
		}
		
		public ReentrantLock getLock() {
			return this.lock;
		}
		
		public void setNext(Node next) {
			this.next = next;
		}
	}

	// class variables for priority queue
	private int capacity;
	private int size;
	private Node head;
	private Condition notEmpty;
	private Condition isFull;

	public PriorityQueue(int maxSize) {
        // Creates a Priority queue with maximum allowed size as capacity
		this.capacity = maxSize;
		this.size = 0;
		this.head = new Node(null, -1);
		this.notEmpty = this.head.getLock().newCondition();
		this.isFull = this.head.getLock().newCondition();
	}

	public int add(String name, int priority) {
        // Adds the name with its priority to this queue.
        // Returns the current position in the 
		// list where the name was inserted;
        // otherwise, returns -1 if the name is already present in the list.
        // This method blocks when the list is full.
		
		// does this name already exist?
		int found = search(name);
		if (found >= 0) { return -1; }
		
		// lock the head
		this.head.getLock().lock();
		int index = 0;
		try {
			// block until there is space to insert elements into the queue
			while(this.size == this.capacity) {
				try { this.isFull.await(); } 
				catch (InterruptedException e) { e.printStackTrace(); }
			}
			
			// add the first element if the queue is empty
			Node node = new Node(name, priority);
			Node curr = this.head.getNext();
			if (curr == null) {
				this.head.setNext(node);
				this.size++;
				return 0;
			}
			
			// iterate through the queue to find where to insert the new node
			Node prev = this.head;
			curr.getLock().lock();
			while (curr != null && curr.getPriority() > priority) {
				if (prev != this.head) { prev.getLock().unlock(); }
				prev = curr;
				curr = curr.getNext();
				if (curr != null) { curr.getLock().lock(); }
				index++;
			}
			
			// insert the new node
			prev.setNext(node);
			node.setNext(curr);
			
			// increment the size
			this.size++;
			
			// unlock any held locks
			if (prev != this.head) { prev.getLock().unlock(); }
			if (curr != null) { curr.getLock().unlock(); }
		} finally {
			// signal to any waiting threads
			this.notEmpty.signalAll();
			
			// unlock the head
			this.head.getLock().unlock();
		}
		
		// return the index of the inserted node
		return index;
	}

	public int search(String name) {
        // Returns the position of the name in the list;
        // otherwise, returns -1 if the name is not found.
		
		// lock the head
		this.head.getLock().lock();
		try {
			// return -1 if the queue is empty
			Node curr = this.head.getNext();
			if (curr == null) {
				return -1; 
			}
			
			// iterate through the queue to see if the name exists
			int index = 0;
			curr.getLock().lock();
			while (curr != null) {
				if (curr.getName().equals(name)) { 
					curr.getLock().unlock();
					return index; 
				}
				Node next = curr.getNext();
				if (next != null) { next.getLock().lock(); }
				curr.getLock().unlock();
				curr = next;
				index++;
			}
		} finally {
			// unlock the head
			this.head.getLock().unlock();
		}
		
		// return -1 if the name does not exist
		return -1;
	}

	public String getFirst() {
        // Retrieves and removes the name with the highest priority in the list,
        // or blocks the thread if the list is empty.
		
		// lock the head
		this.head.getLock().lock();
		String name = "";
		try {	
			// block until the queue is non-empty
			while(this.size == 0) {
				try { this.notEmpty.await(); } 
				catch (InterruptedException e) { e.printStackTrace(); }
			}
			
			// obtain the first element and get its name
			Node first = this.head.getNext();
			first.getLock().lock();
			name = first.getName();
			
			// decrement the size of the queue
			this.size--;
			
			// update the queue
			this.head.setNext(first.getNext());
			
			// unlock the removed node
			first.getLock().unlock();
		} finally {
			// signal any waiting threads
			this.isFull.signalAll();
			
			// unlock the head
			this.head.getLock().unlock();
		}
		
		// return the name
		return name;
	}
}
