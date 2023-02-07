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
        // Returns the current position in the list where the name was inserted;
        // otherwise, returns -1 if the name is already present in the list.
        // This method blocks when the list is full.
		
		// block thread if the queue is full
		if (this.size >= this.capacity) {
			try { this.isFull.await(); } 
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		// helper variables
		Node node = new Node(name, priority);
		Node curr = this.head;
		Node prev = null;
		int index = -1;
		
		try {
			// hand-over-hand locking
			curr.getLock().lock();
			
			// insert new node into queue
			while (curr.getNext() != null && curr.getNext().getPriority() > priority) {
				prev = curr;
				curr = curr.getNext();
				prev.getLock().unlock();
				curr.getLock().lock();
				index++;
			}
			node.setNext(curr.getNext());
			curr.setNext(node);
		} finally {
			// unlock all nodes and update size
			curr.getLock().unlock();
			this.size++;
			
			// unblock waiting threads waiting to retrieve elements
			this.notEmpty.notifyAll();
		}
		
		// return index where the new node was inserted into queue
		return index;
	}

	public int search(String name) {
        // Returns the position of the name in the list;
        // otherwise, returns -1 if the name is not found.
		
		// helper variables
		Node curr = this.head;
		Node prev = null;
		int index = -1;
		
		try {
			// hand-over-hand locking
			curr.getLock().lock();
			
			// iterate over nodes to find node with same name
			while (curr.getNext() != null) {
				if (curr.getName().equals(name)) {
					return index;
				}
				
				prev = curr;
				curr = curr.getNext();
				prev.getLock().unlock();
				curr.getLock().lock();
				index++;
			}
		} finally {
			// unlock all nodes
			curr.getLock().unlock();
		}
		
		// the name was not found in the list
		return -1;
	}

	public String getFirst() {
        // Retrieves and removes the name with the highest priority in the list,
        // or blocks the thread if the list is empty.
		
		// lock head of queue
		this.head.getLock().lock();
		
		// block thread given empty queue
		while(this.head.getNext() == null) {
			try { this.notEmpty.await(); } 
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		// retrieve the name of the previous head of the queue and update queue
		Node oldHead = this.head.getNext();
		oldHead.getLock().lock();
		String name = oldHead.getName();
		this.head.setNext(oldHead.getNext());
		
		// unlock nodes to allow other threads to access the queue
		oldHead.getLock().unlock();
		this.head.getLock().unlock();
		
		// unblock threads waiting to insert new elements
		this.isFull.notifyAll();
		
		// return the name of the previous head of the queue
		return name;
	}
}