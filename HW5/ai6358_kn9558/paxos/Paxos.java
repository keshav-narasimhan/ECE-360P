// kn9558
// ai6358

package paxos;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is the main class you need to implement paxos instances.
 * It corresponds to a single Paxos peer.
 */

public class Paxos implements PaxosRMI, Runnable {
    ReentrantLock mutex;
    String[] peers; // hostnames of all peers
    int[] ports; // ports of all peers
    int me; // this peer's index into peers[] and ports[]

    Registry registry;
    PaxosRMI stub;

    AtomicBoolean dead; // for testing
    AtomicBoolean unreliable; // for testing

    // variables
    HashMap<Long, Integer> threadMapper;
    HashMap<Integer, HelperInfo> seqMapper;

    int done;
    static int numProposals = 0;
    static boolean wait = false;
    
    // helper class
    class HelperInfo {
    	private State state;
    	private Object value;
    	private ArrayList<Integer> proposalNumbers;
    	private ArrayList<Object> proposalValues;
    	private ArrayList<Integer> acceptNumbers;
    	private Object acceptValue;
    	
		public HelperInfo(State state, Object value, ArrayList<Integer> proposalNumbers,
				ArrayList<Object> proposalValues, ArrayList<Integer> acceptNumbers, Object acceptValue) {
			this.state = state;
			this.value = value;
			this.proposalNumbers = proposalNumbers;
			this.proposalValues = proposalValues;
			this.acceptNumbers = acceptNumbers;
			this.acceptValue = acceptValue;
		}

		public State getState() {
			return state;
		}

		public void setState(State state) {
			this.state = state;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}

		public ArrayList<Integer> getProposalNumbers() {
			return proposalNumbers;
		}

		public void setProposalNumbers(ArrayList<Integer> proposalNumbers) {
			this.proposalNumbers = proposalNumbers;
		}

		public ArrayList<Object> getProposalValues() {
			return proposalValues;
		}

		public void setProposalValues(ArrayList<Object> proposalValues) {
			this.proposalValues = proposalValues;
		}

		public ArrayList<Integer> getAcceptNumbers() {
			return acceptNumbers;
		}

		public void setAcceptNumbers(ArrayList<Integer> acceptNumbers) {
			this.acceptNumbers = acceptNumbers;
		}

		public Object getAcceptValue() {
			return acceptValue;
		}

		public void setAcceptValue(Object acceptValue) {
			this.acceptValue = acceptValue;
		}
    }

    /**
     * Call the constructor to create a Paxos peer.
     * The hostnames of all the Paxos peers (including this one)
     * are in peers[]. The ports are in ports[].
     */
    public Paxos(int me, String[] peers, int[] ports) {
        this.me = me;
        this.peers = peers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.dead = new AtomicBoolean(false);
        this.unreliable = new AtomicBoolean(false);

        // Your initialization code here
        this.threadMapper = new HashMap<>();
        this.seqMapper = new HashMap<>();
        this.done = -1;

        // register peers, do not modify this part
        try {
            System.setProperty("java.rmi.server.hostname", this.peers[this.me]);
            registry = LocateRegistry.createRegistry(this.ports[this.me]);
            stub = (PaxosRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("Paxos", stub);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Call() sends an RMI to the RMI handler on server with
     * arguments rmi name, request message, and server id. It
     * waits for the reply and return a response message if
     * the server responded, and return null if Call() was not
     * be able to contact the server.
     *
     * You should assume that Call() will time out and return
     * null after a while if it doesn't get a reply from the server.
     *
     * Please use Call() to send all RMIs and please don't change
     * this function.
     */
    public Response Call(String rmi, Request req, int id) {
        Response callReply = null;

        PaxosRMI stub;
        try {
            Registry registry = LocateRegistry.getRegistry(this.ports[id]);
            stub = (PaxosRMI) registry.lookup("Paxos");
            if (rmi.equals("Prepare"))
                callReply = stub.Prepare(req);
            else if (rmi.equals("Accept"))
                callReply = stub.Accept(req);
            else if (rmi.equals("Decide"))
                callReply = stub.Decide(req);
            else
                System.out.println("Wrong parameters!");
        } catch (Exception e) {
            return null;
        }
        return callReply;
    }

    /**
     * The application wants Paxos to start agreement on instance seq,
     * with proposed value v. Start() should start a new thread to run
     * Paxos on instance seq. Multiple instances can be run concurrently.
     *
     * Hint: You may start a thread using the runnable interface of
     * Paxos object. One Paxos object may have multiple instances, each
     * instance corresponds to one proposed value/command. Java does not
     * support passing arguments to a thread, so you may reset seq and v
     * in Paxos object before starting a new thread. There is one issue
     * that variable may change before the new thread actually reads it.
     * Test won't fail in this case.
     *
     * Start() just starts a new thread to initialize the agreement.
     * The application will call Status() to find out if/when agreement
     * is reached.
     */
    public void Start(int seq, Object value) {
        // Your code here
        Thread thread = new Thread(this);
        
        // initialize fields for HelperInfo class
        State st = State.Pending;
        Object v = null;
        ArrayList<Integer> pn = new ArrayList<>();
        pn.add(numProposals);
        numProposals++;
        pn.add(-1);
        ArrayList<Object> pv = new ArrayList<>();
        pv.add(value);
        pv.add(-1);
        ArrayList<Integer> an = new ArrayList<>();
        an.add(-1);
        an.add(-1);
        Object av = null;
        
        // create HelperInfo object
        HelperInfo hi = new HelperInfo(st, v, pn, pv, an, av);
        
        // add to sequence mapper data structure
        this.seqMapper.put(seq, hi);
        
        // add to thread mapper data structure
        this.threadMapper.put(thread.getId(), seq);

        // run the thread
        thread.start();
    }

    // RMI Handler for prepare requests
    public Response Prepare(Request req) {
        // your code here
        if (req.str != null && req.str.equals("done")) {
        	Response done = new Response(this.done);
            return done;
        } 

        int seq = req.seq;
        if (this.seqMapper.containsKey(seq) == false) {
        	// initialize fields for HelperInfo class
            State st = State.Pending;
            Object v = null;
            ArrayList<Integer> an = new ArrayList<>();
            an.add(-1);
            an.add(-1);
            Object av = null;
            
            // create HelperInfo object
            HelperInfo hi = new HelperInfo(st, v, null, null, an, av);
            
            // add to sequence mapper data structure
            this.seqMapper.put(seq, hi);
            
            // add to thread mapper data structure
            this.threadMapper.put(Thread.currentThread().getId(), seq);
        }

        HelperInfo hi = this.seqMapper.get(seq);
        int hp = hi.getAcceptNumbers().get(0);
        int ha = hi.getAcceptNumbers().get(1);
        Object hav = hi.getAcceptValue();


        if(req.proposal > hp) {
            hi.getAcceptNumbers().set(0, req.proposal);
            Response ack = new Response(true, req.proposal, ha, hav);
            return ack;
        }
        else {
            Response nack = new Response(false, req.proposal, ha, hav);
            return nack;
        }
    }

    // RMI Handler for accept requests
    public Response Accept(Request req) {
        // your code here
        int seq = req.seq;
        HelperInfo hi = this.seqMapper.get(seq);
        int biggest = hi.getAcceptNumbers().get(0);
        int proposal = req.proposal;
        Object value = req.value;

        if(proposal >= biggest) {
            hi.getAcceptNumbers().set(0, proposal);
            hi.getAcceptNumbers().set(1, proposal);
            hi.setAcceptValue(value);
            Response ack = new Response(true, proposal,  hi.getAcceptNumbers().get(1), hi.getAcceptValue());
            return ack;
        }
        else {
            Response nack = new Response(false, -1, -1, null);
            return nack;
        }
    }

    // RMI Handler for decide requests
    public Response Decide(Request req) {
        // your code 
    	int seq = req.seq;
    	HelperInfo hi = this.seqMapper.get(seq);
    	
    	hi.setState(State.Decided);
    	hi.setValue(req.value);
        return null;
    }

    /**
     * The application on this machine is done with
     * all instances <= seq.
     *
     * see the comments for Min() for more explanation.
     */
    public void Done(int seq) {
        // Your code here
    	HashSet<Integer> allLessSeqValues = new HashSet<>();
    	for (int s : this.seqMapper.keySet()) {
    		if (s <= seq) {
    			allLessSeqValues.add(s);
    		}
    	}
    	
    	HashSet<Long> allLessThreadValues = new HashSet<>();
    	for (long l : this.threadMapper.keySet()) {
    		if (this.threadMapper.get(l) <= seq) {
    			allLessThreadValues.add(l);
    		}
    	}
    	
    	this.seqMapper.keySet().removeAll(allLessSeqValues);
    	this.threadMapper.keySet().removeAll(allLessThreadValues);
    	
        this.done = seq;
        wait = true;
    }

    /**
     * The application wants to know the
     * highest instance sequence known to
     * this peer.
     */
    public int Max() {
        // Your code here
    	int max = Integer.MIN_VALUE;
    	for (int val : this.threadMapper.values()) {
    		if (val > max) {
    			max = val;
    		}
    	}
    	
    	return max;
    }

    /**
     * Min() should return one more than the minimum among z_i,
     * where z_i is the highest number ever passed
     * to Done() on peer i. A peers z_i is -1 if it has
     * never called Done().

     * Paxos is required to have forgotten all information
     * about any instances it knows that are < Min().
     * The point is to free up memory in long-running
     * Paxos-based servers.

     * Paxos peers need to exchange their highest Done()
     * arguments in order to implement Min(). These
     * exchanges can be piggybacked on ordinary Paxos
     * agreement protocol messages, so it is OK if one
     * peers Min does not reflect another Peers Done()
     * until after the next instance is agreed to.

     * The fact that Min() is defined as a minimum over
     * all Paxos peers means that Min() cannot increase until
     * all peers have been heard from. So if a peer is dead
     * or unreachable, other peers Min()s will not increase
     * even if all reachable peers call Done. The reason for
     * this is that when the unreachable peer comes back to
     * life, it will need to catch up on instances that it
     * missed -- the other peers therefore cannot forget these
     * instances.
     */
    public int Min() {
        // Your code here
        int min = Integer.MAX_VALUE;
        int length = this.peers.length;
        
        for(int i = 0; i < length; i++) {
        	Request done = new Request("done");
        	Response response = this.Call("Prepare", done, i);
        	int d = response.done;
        	
            if (d < min) {
            	min = d;
            }
        }
        
        return min + 1;
    }

    /**
     * The application wants to know whether this
     * peer thinks an instance has been decided,
     * and if so what the agreed value is. Status()
     * should just inspect the local peer state;
     * it should not contact other Paxos peers.
     */
    public retStatus Status(int seq) {
        // Your code here
    	if (this.seqMapper.containsKey(seq) == false) {
    		retStatus nl = new retStatus(null, null);
    		return nl;
    	}
    	
    	HelperInfo hi = this.seqMapper.get(seq);
    	retStatus rs = new retStatus(hi.getState(), hi.getValue());
    	return rs;
    }
    
    @Override
    public void run() {
        // Your code here
        int seq = this.threadMapper.get(Thread.currentThread().getId());
        HelperInfo hi = this.seqMapper.get(seq);
        
        int proposal = hi.getProposalNumbers().get(0);
        int biggest = hi.getProposalNumbers().get(1);
        Object value = hi.getProposalValues().get(0);
        int length = this.peers.length;
        int ar = 0;
        int pr = 0;
        
        while (hi.getState() != State.Decided) {
            int index = 0;
            while (index < length) {
                Response response = this.Call("Prepare", new Request(proposal, value, seq), index);
                if (response == null) {
                	index++;
                	continue;
                }
                
                if (response.b == true) {
                	pr++;
                	
                	if (response.biggestNum > biggest && response.biggestVal != null) {
                		value = response.biggestVal;
                	}
                }
                
                index++;
        	}
            
            // if majority are not proposed --> continue
            if (pr <= length / 2) {
            	continue;
            }
            
            index = 0;
            while (index < length) {
                Request req = new Request(proposal, value, seq);
                Response response = this.Call("Accept", req, index);
                    
                if (response == null) {
                    index++;
                    continue;
                }
                    
                if (response.b == true) {
                	ar++;
                }
                    
                index++;
            }
            
            // if majority are not accepted --> continue
            if (ar <= length / 2) {
            	continue;
            }
                
            index = 0;
            while (index < length) {
            	Request req = new Request(proposal, value, seq);
            	this.Call("Decide", req, index);
                index++;
            }
                 
            // set the new state and values
            hi.setState(State.Decided);
            hi.setValue(value);
        }
    }

    /**
     * helper class for Status() return
     */
    public class retStatus {
        public State state;
        public Object v;

        public retStatus(State state, Object v) {
            this.state = state;
            this.v = v;
        }
    }

    /**
     * Tell the peer to shut itself down.
     * For testing.
     * Please don't change these four functions.
     */
    public void Kill() {
        this.dead.getAndSet(true);
        if (this.registry != null) {
            try {
                UnicastRemoteObject.unexportObject(this.registry, true);
            } catch (Exception e) {
                System.out.println("None reference");
            }
        }
    }

    public boolean isDead() {
        return this.dead.get();
    }

    public void setUnreliable() {
        this.unreliable.getAndSet(true);
    }

    public boolean isunreliable() {
        return this.unreliable.get();
    }
}

