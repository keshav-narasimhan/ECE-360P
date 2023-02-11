// kn9558
// ai6358

/* Use only Java monitors to accomplish the required synchronization */
public class MonitorCyclicBarrier implements CyclicBarrier {

    private int parties;
    
    // new class variables
    private int count;
    private boolean isActive;
    private Object monitor;

    public MonitorCyclicBarrier(int parties) {
        this.parties = parties;
        
        // initialize custom variables
        this.count = 0;
        this.isActive = true;
        this.monitor = new Object();
    }

    /*
     * An active CyclicBarrier waits until all parties have invoked
     * await on this CyclicBarrier. If the current thread is not
     * the last to arrive then it is disabled for thread scheduling
     * purposes and lies dormant until the last thread arrives.
     * An inactive CyclicBarrier does not block the calling thread. It
     * instead allows the thread to proceed by immediately returning.
     * Returns: the arrival index of the current thread, where index 0
     * indicates the first to arrive and (parties-1) indicates
     * the last to arrive.
     */
    public int await() throws InterruptedException {
    	// synchronize on monitor
    	synchronized(this.monitor) {
    		// return -1 if deactivated
    		if (!this.isActive) {
        		return -1;
        	}
        	
    		// find arrival index and update count
        	int arrivalIndex = this.count;
            this.count++;
            
            // release all threads if reached threshold, or else block
            if(this.count == this.parties) {
                this.count = 0;
                this.monitor.notifyAll();
            }
            else {
            	this.monitor.wait();
            }
            
            // return the arrival index
            return arrivalIndex;
    	}
    }

    /*
     * This method activates the cyclic barrier. If it is already in
     * the active state, no change is made.
     * If the barrier is in the inactive state, it is activated and
     * the state of the barrier is reset to its initial value.
     */
    public void activate() throws InterruptedException {
    	// synchronize on monitor
        synchronized(this.monitor) {
        	// return if deactivated
        	if (this.isActive) {
        		return;
        	}
        	
        	// activate the barrier
        	this.count = 0;
        	this.isActive = true;
        }
    }

    /*
     * This method deactivates the cyclic barrier.
     * It also releases any waiting threads
     */
    public void deactivate() throws InterruptedException {
    	// synchronize on monitor
        synchronized(this.monitor) {
        	// return if deactivated
        	if (!this.isActive) {
        		return;
        	}
        	
        	// deactivate the monitor and release any blocked threads
        	this.isActive = false;
        	this.monitor.notifyAll();
        }
    }
}