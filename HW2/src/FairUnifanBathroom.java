// kn9558
// ai6358

public class FairUnifanBathroom { 
	private static int numUT = 0;
	private static int numOU = 0;
	private int ticketNumber;
	private static int counter = 1;
	private static int currentTicket = 1;
	
	public FairUnifanBathroom(int ticketNumber) {
		ticketNumber = counter;
		counter++;
	}
	
	public synchronized void enterBathroomUT() {
		// Called when a UT fan wants to enter bathroom	
		while (numOU > 0 || numUT == 7 || ticketNumber != currentTicket) {
			try { wait(); } 
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		numUT++;
	}
	
	public synchronized void enterBathroomOU() {
		// Called when a OU fan wants to enter bathroom
		while (numOU == 7 || numUT > 0 || ticketNumber != currentTicket) {
			try { wait(); } 
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		numOU++;
	}
	
	public synchronized void leaveBathroomUT() {
		// Called when a UT fan wants to leave bathroom
		numUT--;
		currentTicket++;
		notifyAll();
	}

	public synchronized void leaveBathroomOU() {
		// Called when a OU fan wants to leave bathroom
		numOU--;
		currentTicket++;
		notifyAll();
	}
}