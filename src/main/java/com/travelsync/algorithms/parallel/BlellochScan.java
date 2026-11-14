package com.travelsync.algorithms.parallel;

import java.util.ArrayList;
import java.util.List;

/**
 * Blelloch (work-efficient) exclusive prefix-sum scan, built from an
 * up-sweep (reduce) phase followed by a down-sweep phase. Used in
 * TravelSync to compute cumulative booking statistics (e.g. running totals
 * of bookings/revenue over time) with the parallel-friendly O(n) work /
 * O(log n) depth structure -- the two sweeps are independent at each level,
 * which is what makes this algorithm parallelizable across p processors.
 *
 * Work complexity: O(n)   Depth (parallel steps): O(log n).
 * Input length is padded up to the next power of two internally.
 */
public final class BlellochScan {

    private BlellochScan() { }

    /** Returns the exclusive prefix sum of input (result[i] = sum of input[0..i-1]). */
    public static long[] exclusiveScan(long[] input) {
        int n = input.length;
        if (n == 0) return new long[0];
        int size = Integer.highestOneBit(n) == n ? n : Integer.highestOneBit(n) * 2;
        long[] a = new long[size];
        System.arraycopy(input, 0, a, 0, n);

        // Up-sweep (reduce phase): builds partial sums level by level.
        for (int d = 0; d < log2(size); d++) {
            int stride = 1 << (d + 1);
            for (int i = stride - 1; i < size; i += stride) {
                a[i] += a[i - (stride >> 1)];
            }
        }

        a[size - 1] = 0; // identity for exclusive scan

        // Down-sweep phase: distributes partial sums back down the tree.
        for (int d = log2(size) - 1; d >= 0; d--) {
            int stride = 1 << (d + 1);
            for (int i = stride - 1; i < size; i += stride) {
                long left = a[i - (stride >> 1)];
                a[i - (stride >> 1)] = a[i];
                a[i] += left;
            }
        }

        long[] result = new long[n];
        System.arraycopy(a, 0, result, 0, n);
        return result;
    }

    /**
     * One recorded phase of the scan.
     *
     * @param phase    "input", "up-sweep", "root" or "down-sweep"
     * @param level    sweep level d (or -1)
     * @param snapshot copy of the (padded) working array afterwards
     * @param touched  indices written during this phase
     * @param partners for every touched index, the index it combined with
     */
    public record Step(String phase, int level, long[] snapshot, int[] touched, int[] partners) { }

    public record Trace(long[] result, int paddedSize, List<Step> steps) { }

    /** Same algorithm as {@link #exclusiveScan}, recording the working array after every sweep level. */
    public static Trace trace(long[] input) {
        int n = input.length;
        int size = n == 0 ? 0 : (Integer.highestOneBit(n) == n ? n : Integer.highestOneBit(n) * 2);
        long[] a = new long[size];
        System.arraycopy(input, 0, a, 0, n);
        List<Step> steps = new ArrayList<>();
        steps.add(new Step("input", -1, a.clone(), new int[0], new int[0]));
        for (int d = 0; d < (size == 0 ? 0 : log2(size)); d++) {
            int stride = 1 << (d + 1);
            List<Integer> touched = new ArrayList<>();
            List<Integer> partners = new ArrayList<>();
            for (int i = stride - 1; i < size; i += stride) {
                a[i] += a[i - (stride >> 1)];
                touched.add(i);
                partners.add(i - (stride >> 1));
            }
            steps.add(new Step("up-sweep", d, a.clone(), toArray(touched), toArray(partners)));
        }
        if (size > 0) {
            a[size - 1] = 0;
            steps.add(new Step("root", -1, a.clone(), new int[]{size - 1}, new int[0]));
        }
        for (int d = (size == 0 ? 0 : log2(size)) - 1; d >= 0; d--) {
            int stride = 1 << (d + 1);
            List<Integer> touched = new ArrayList<>();
            List<Integer> partners = new ArrayList<>();
            for (int i = stride - 1; i < size; i += stride) {
                long left = a[i - (stride >> 1)];
                a[i - (stride >> 1)] = a[i];
                a[i] += left;
                touched.add(i);
                partners.add(i - (stride >> 1));
            }
            steps.add(new Step("down-sweep", d, a.clone(), toArray(touched), toArray(partners)));
        }
        long[] result = new long[n];
        System.arraycopy(a, 0, result, 0, n);
        return new Trace(result, size, steps);
    }

    private static int[] toArray(List<Integer> list) {
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    private static int log2(int n) {
        return 31 - Integer.numberOfLeadingZeros(n);
    }
}
