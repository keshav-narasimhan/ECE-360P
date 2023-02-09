import java.util.concurrent.Semaphore;

// EID 1
// EID 2


/* Use only semaphores to accomplish the required synchronization */
public class SemaphoreCyclicBarrier implements CyclicBarrier {
	// given class variable
    private int parties;

    // made new variables
    private int index;
    private int count;
    private boolean isActive;
    private Semaphore mutex;
    private Semaphore sema1;
    private Semaphore sema2;
    private boolean isSema1;

    public SemaphoreCyclicBarrier(int parties) {
        this.parties = parties;
        
        // initialize custom variables
        this.count = 0;
        this.isActive = true;
        this.mutex = new Semaphore(1);
        this.sema1 = new Semaphore(0);
        this.sema2 = new Semaphore(0);
        this.isSema1 = true;
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
    	// acquire the semaphore
    	this.mutex.acquire();
    	
    	// inactive CyclicBarrier - release thread
    	if (!this.isActive) {
    		this.mutex.release();
    		return -1;
    	}
    	
    	// get the arrival index and update the count
    	int arrivalIndex = this.count;
    	this.count++;
    	
    	// release threads if reached the barrier
    	if (this.count == this.parties) {
    		this.count = 0;
    		if (this.isSema1) {
    			this.sema1.release(this.parties - 1);
    		} else {
    			this.sema2.release(this.parties - 1);
    		}
    		this.isSema1 = !this.isSema1;
    		this.mutex.release();
    	} 
    	// block the thread if barrier quantity hasn't reached yet
    	else {
    		this.mutex.release();
    		if (this.isSema1) {
    			this.sema1.acquire();
    		} else {
    			this.sema2.acquire();
    		}
    	}
    	
    	// return the index
        return arrivalIndex;
    }

    /*
     * This method activates the cyclic barrier. If it is already in
     * the active state, no change is made.
     * If the barrier is in the inactive state, it is activated and
     * the state of the barrier is reset to its initial value.
     */
    public void activate() throws InterruptedException {
        // acquire the mutex
    	this.mutex.acquire();
        
    	// do nothing if already active
        if(this.isActive) {
        	this.mutex.release();
        	return;
        }
        
        // set to active and release the mutex
        this.isActive = true;
        this.count = 0;
        this.mutex.release();
    }

    /*
     * This method deactivates the cyclic barrier.
     * It also releases any waiting threads
     */
    public void deactivate() throws InterruptedException {
        // acquire the mutex
    	this.mutex.acquire();
        
    	// do nothing if already inactive
        if (!this.isActive) {
        	this.mutex.release();
        	return;
        }
        
        // set to inactive
        this.isActive = false;
        
        // release any waiting threads and release the mutex
        if (this.count > 0) {
        	if (this.isSema1) {
        		this.sema1.release(this.count);
        	} else {
        		this.sema2.release(this.count);
        	}
        }
        this.mutex.release();
    }
}