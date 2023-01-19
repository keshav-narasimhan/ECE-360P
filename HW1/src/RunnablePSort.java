//UT-EID=kn9558

import java.util.*;
import java.util.concurrent.*;

public class RunnablePSort {
    /* Notes:
     * The input array (A) is also the output array,
     * The range to be sorted extends from index begin, inclusive, to index end, exclusive,
     * Sort in increasing order when increasing=true, and decreasing order when increasing=false,
     */
    public static void parallelSort(int[] A, int begin, int end, boolean increasing) {
        // TODO: Implement your parallel sort function using Runnables
    	QuickSortRunnable quicksort = new QuickSortRunnable(A, begin, end, increasing);
    	Thread t = new Thread(quicksort);
    	t.start();
    	
    	try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}

class QuickSortRunnable implements Runnable {
	int[] array;
	int begin;
	int end;
	boolean increasing;
	
	public QuickSortRunnable(int[] array, int begin, int end, boolean increasing) {
		this.array = array;
		this.begin = begin;
		this.end = end;
		this.increasing = increasing;
	}
	
	private int findPartitionDec() {
		int pivot = this.array[this.end - 1];
		int lowIndex = this.begin;
		int highIndex = this.end - 2;
		
		while (lowIndex < highIndex) {
			if (this.array[highIndex] < pivot) {
				highIndex -= 1;
			} else {
				if (this.array[lowIndex] >= pivot) {
					lowIndex += 1;
				} else {
					int swap = this.array[highIndex];
					this.array[highIndex] = this.array[lowIndex];
					this.array[lowIndex] = swap;
					lowIndex += 1;
					highIndex -= 1;
				}
			}
		}
		
		while (this.array[lowIndex] < pivot) {
			if (lowIndex == this.begin) break;
			lowIndex -= 1;
		}
		
		if (lowIndex != this.end - 1 && lowIndex != this.begin) {
			int swap = this.array[lowIndex + 1];
			this.array[lowIndex + 1] = pivot;
			this.array[this.end - 1] = swap;
			return lowIndex + 1;
		} else if (lowIndex == this.begin) {
			int swap = this.array[lowIndex];
			this.array[lowIndex] = pivot;
			this.array[this.end - 1] = swap;
			return lowIndex;
		} else {
			return lowIndex;
		}
	}
	
	private int findPartitionInc() {
		int pivot = this.array[this.end - 1];
		int lowIndex = this.begin;
		int highIndex = this.end - 2;
		
		while (lowIndex < highIndex) {
			if (this.array[highIndex] < pivot) {
				if (this.array[lowIndex] <= pivot) {
					lowIndex += 1;
				} else {
					int swap = this.array[highIndex];
					this.array[highIndex] = this.array[lowIndex];
					this.array[lowIndex] = swap;
					lowIndex += 1;
					highIndex -= 1;
				}
			} else {
				highIndex -= 1;
			}
		}
		
		while (this.array[lowIndex] > pivot) {
			if (lowIndex == this.begin) break;
			lowIndex -= 1;
		}
		
		if (lowIndex != this.end - 1 && lowIndex != this.begin) {
			int swap = this.array[lowIndex + 1];
			this.array[lowIndex + 1] = pivot;
			this.array[this.end - 1] = swap;
			return lowIndex + 1;
		} else if (lowIndex == this.begin) {
			int swap = this.array[lowIndex];
			this.array[lowIndex] = pivot;
			this.array[this.end - 1] = swap;
			return lowIndex;
		} else {
			return lowIndex;
		}
	}

	@Override
	public void run() {
		if (this.end - this.begin <= 16) {
			int[] arraycopy = Arrays.copyOfRange(this.array, this.begin, this.end);
			if (increasing) {
				Arrays.sort(arraycopy);
			} else {
				for (int i = 0; i < arraycopy.length; i++) {
					arraycopy[i] *= -1;
				}
				Arrays.sort(arraycopy);
				for (int i = 0; i < arraycopy.length; i++) {
					arraycopy[i] *= -1;
				}
			}
			for (int i = this.begin; i < this.end; i++) {
				this.array[i] = arraycopy[i - this.begin];
			}
		} else if (this.begin < this.end) {
			int pivot = this.increasing ? findPartitionInc() : findPartitionDec();
			if (pivot == this.begin) {
				QuickSortRunnable qsr = new QuickSortRunnable(this.array, pivot + 1, this.end, this.increasing);
				Thread t = new Thread(qsr);
				t.start();
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else if (pivot == this.end - 1) {
				QuickSortRunnable qsr = new QuickSortRunnable(this.array, this.begin, pivot, this.increasing);
				Thread t = new Thread(qsr);
				t.start();
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				QuickSortRunnable qsleft = new QuickSortRunnable(this.array, this.begin, pivot + 1, this.increasing);
				QuickSortRunnable qsright = new QuickSortRunnable(this.array, pivot + 1, this.end, this.increasing);
				Thread tleft = new Thread(qsleft);
				Thread tright = new Thread(qsright);
				tleft.start();
				tright.start();
				try {
					tleft.join();
					tright.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}