//UT-EID=kn9558, ai6358

import java.util.*;
import java.util.concurrent.*;

public class ForkJoinPSort {
    /* Notes:
     * The input array (A) is also the output array,
     * The range to be sorted extends from index begin, inclusive, to index end, exclusive,
     * Sort in increasing order when increasing=true, and decreasing order when increasing=false,
     */
	
    public static void parallelSort(int[] A, int begin, int end, boolean increasing) {
        // TODO: Implement your parallel sort function using ForkJoinPool
    	int processors = Runtime.getRuntime().availableProcessors();
    	ForkJoinPool pool = new ForkJoinPool(processors);
    	QuickSortFork quicksort = new QuickSortFork(A, begin, end, increasing);
    	pool.invoke(quicksort);
    }
}

class QuickSortFork extends RecursiveTask<Void> {
	int[] array;
	int begin;
	int end;
	boolean increasing;
	
	public QuickSortFork(int[] array, int begin, int end, boolean increasing) {
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
	protected Void compute() {
		if (this.end - this.begin < 16) {
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
				QuickSortFork qsf = new QuickSortFork(this.array, pivot + 1, this.end, this.increasing);
				qsf.fork();
				qsf.join();
			} else if (pivot == this.end - 1) {
				QuickSortFork qsf = new QuickSortFork(this.array, this.begin, pivot, this.increasing);
				qsf.fork();
				qsf.join();
			} else {
				QuickSortFork qsleft = new QuickSortFork(this.array, this.begin, pivot + 1, this.increasing);
				qsleft.fork();
				QuickSortFork qsright = new QuickSortFork(this.array, pivot + 1, this.end, this.increasing);
				qsright.fork();
				qsleft.join();
				qsright.join();
			}
		}
		return null;
	}
	
}