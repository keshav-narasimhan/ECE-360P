//UT-EID=kn9558, ai6358

import java.util.*;
import java.util.concurrent.*;

public class PMerge {
    /* Notes:
     * Arrays A and B are sorted in the ascending order
     * These arrays may have different sizes.
     * Array C is the merged array sorted in the descending order
     */
    public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads) {
        // TODO: Implement your parallel merge function
    	if (C.length < A.length + B.length) {
    		System.out.println("Ensure that C is large enough!");
    		return;
    	}
    	
    	ExecutorService es = Executors.newFixedThreadPool(numThreads);
    	for (int i = 0; i < A.length; i++) {
    		es.submit(new MergeHelper(B, C, i, A[i], true));
    	}
    	for (int i = 0; i < B.length; i++) {
    		es.submit(new MergeHelper(A, C, i, B[i], false));
    	}
    	
    	es.shutdown();
    	while(!es.isTerminated());
    }
    
}

class MergeHelper implements Callable<Integer> {
	int[] A;
	int[] C;
	int ind;
	int target;
	boolean findMaxPos;
	
	public MergeHelper(int[] A, int[] C, int index, int target, boolean b) {
		this.A = A;
		this.C = C;
		this.ind = index;
		this.target = target;
		this.findMaxPos = b;
	}
	
	public Integer call() {
		int newIndex = ind + getLocation();
		newIndex = C.length - newIndex - 1;
		C[newIndex] = this.target;
		return 0;
	}
	
	public int getLocation() {
		int low = 0;
		int high = A.length - 1;
		int index = (low + high) / 2;
		int pindex = -1;
		while (index >= 0 && index < A.length) {
			if (A[index] == this.target) {
				if(this.findMaxPos) {
					index += 1;
					while(index < A.length && A[index] == target) {
						index += 1;
					}
					return index;
				} else {
					index -= 1;
					while(index >= 0 && A[index] == target) {
						index -= 1;
					}
					return index + 1;
				}
			} else if (pindex == index) {
				return A[index] > this.target ? index : index + 1;
			} else if (A[index] < this.target) {
				low = index + 1;
				pindex = index;
				index = (low + high) / 2;
			} else {
				high = index - 1;
				pindex = index;
				index = (low + high) / 2;
			}
		}
		
		return index;
	}
}