// kn9558
// ai6358

package paxos;

import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the response message for each RMI call.
 * Hint: You may need a boolean variable to indicate ack of acceptors and also you may need proposal number and value.
 * Hint: Make it more generic such that you can use it for each RMI call.
 */
public class Response implements Serializable {
    static final long serialVersionUID = 2L;

    // Your data here
    boolean b;
    int biggestNum;
    Object biggestVal;
    int proposal;
    int done;

    // Your constructor and methods here
    public Response(int done) {
        this.done = done;
    }
    
    public Response(boolean b, int proposal, int biggestNum, Object biggestVal) {
        this.b = b;
        this.proposal = proposal;
        this.biggestNum = biggestNum;
        this.biggestVal = biggestVal;
        this.done = -1;
    }
}

