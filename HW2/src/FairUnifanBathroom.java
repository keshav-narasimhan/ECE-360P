// kn9558
// ai6358

public class FairUnifanBathroom { 
	static int numUT = 0;
	static int numOU = 0;
	static int counter = 1;
	static int currentTicket = 1;
	
	public synchronized void enterBathroomUT() {
		// Called when a UT fan wants to enter bathroom	
		
		// get the current ticket number
		int ticketNumber = counter;
		counter++;
		
		// block till bathroom is free and it's the thread's turn
		while (numOU > 0 || numUT == 7 || ticketNumber != currentTicket) {
			try { wait(); } 
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		// increment the number of UT students in the bathroom
		numUT++;
	}
	
	public synchronized void enterBathroomOU() {
		// Called when a OU fan wants to enter bathroom
		
		// get the current ticket number
		int ticketNumber = counter;
		counter++;
		
		// block till bathroom is free and it's the thread's turn
		while (numOU == 7 || numUT > 0 || ticketNumber != currentTicket) {
			try { wait(); } 
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		// increment the number of OU students in the bathroom
		numOU++;
	}
	
	public synchronized void leaveBathroomUT() {
		// Called when a UT fan wants to leave bathroom
		
		// do nothing if there are no UT students in the bathroom
		if (numUT == 0) { return; }
		
		// decrement the number of UT students and get the next ticket
		numUT--;
		currentTicket++;
		notifyAll();
	}

	public synchronized void leaveBathroomOU() {
		// Called when a OU fan wants to leave bathroom
		
		// do nothing if there are no OU students in the bathroom
		if (numOU == 0) { return; }
		
		// decrement the number of OU students and get the next ticket
		numOU--;
		currentTicket++;
		notifyAll();
	}
}
