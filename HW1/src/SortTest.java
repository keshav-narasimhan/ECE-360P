import java.util.Arrays;

public class SortTest {
    public static void main(String[] args) {
        int[] A1 = { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
        verifyParallelSort(A1);

        int[] A2 = { 1, 3, 5, 7, 9 };
        verifyParallelSort(A2);

        int[] A3 = { 13, 59, 24, 18, 33, 20, 11, 11, 13, 50, 10999, 97 };
        verifyParallelSort(A3);
        
        int[] A4 = { 19292, 229, 29394, 2922, 122, 33, 2, 3, 55, 293934, 2, 3393, 223, 222, 0, 1, 800, 5, 300000, 2 };
        verifyParallelSort(A4);
        
        int[] A5 = { 35, 90, 2884829, 31, 0, 1, 9299492, 299293, 892, 2394929, 8901, 91, 89, 34, 8618, 190, 7, 9, 1002, 2201, 1010, 11, 12, 97, 101, 901, 904, 409, 2020, 394, 34, 36, 38, 4, 9, 99, 999, 99999, 9999, 9220, 791 };
        verifyParallelSort(A5);
    }

    static void verifyParallelSort(int[] A) {
        int[] sorted = new int[A.length];
        int[] B = new int[A.length];

        System.arraycopy(A, 0, sorted, 0, A.length);
        Arrays.sort(sorted);
        System.arraycopy(A, 0, B, 0, A.length);

        System.out.println("Verify Parallel Sort for array: ");
        printArray(A);

        boolean isSuccess = true;

        RunnablePSort.parallelSort(B, 0, B.length, true);
        for (int i = 0; i < sorted.length; i++) {
            if (sorted[i] != B[i]) {
                System.out.println("Your parallel sorting algorithm (runnable, increasing) is not correct");
                System.out.println("Expect:");
                printArray(sorted);
                System.out.println("Your results:");
                printArray(B);
                isSuccess = false;
                break;
            }
        }

        System.arraycopy(A, 0, B, 0, A.length);
        ForkJoinPSort.parallelSort(B, 0, B.length, true);
        for (int i = 0; i < sorted.length; i++) {
            if (sorted[i] != B[i]) {
                System.out.println("Your parallel sorting algorithm (forkjoin, increasing) is not correct");
                System.out.println("Expect:");
                printArray(sorted);
                System.out.println("Your results:");
                printArray(B);
                isSuccess = false;
                break;
            }
        }

        // Reverse sorted array for decreasing tests
        for (int left = 0, right = sorted.length - 1; left < right; left++, right--) {
            int temp = sorted[left];
            sorted[left] = sorted[right];
            sorted[right] = temp;
        }

        System.arraycopy(A, 0, B, 0, A.length);
        RunnablePSort.parallelSort(B, 0, B.length, false);
        for (int i = 0; i < sorted.length; i++) {
            if (sorted[i] != B[i]) {
                System.out.println("Your parallel sorting algorithm (runnable, decreasing) is not correct");
                System.out.println("Expect:");
                printArray(sorted);
                System.out.println("Your results:");
                printArray(B);
                isSuccess = false;
                break;
            }
        }

        System.arraycopy(A, 0, B, 0, A.length);
        ForkJoinPSort.parallelSort(B, 0, B.length, false);
        for (int i = 0; i < sorted.length; i++) {
            if (sorted[i] != B[i]) {
                System.out.println("Your parallel sorting algorithm (forkjoin, decreasing) is not correct");
                System.out.println("Expect:");
                printArray(sorted);
                System.out.println("Your results:");
                printArray(B);
                isSuccess = false;
                break;
            }
        }

        if (isSuccess) {
            System.out.println("Great, your sorting algorithm works for this test case");
        }
        System.out.println("=========================================================");
    }

    public static void printArray(int[] A) {
        for (int i = 0; i < A.length; i++) {
            if (i != A.length - 1) {
                System.out.print(A[i] + " ");
            } else {
                System.out.print(A[i]);
            }
        }
        System.out.println();
    }
}