package com.travelsync.algorithms.parallel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Randomized QuickSort: picks a uniformly random pivot at each partition
 * step, giving expected O(n log n) time regardless of input order (avoiding
 * the O(n^2) worst case of a fixed-pivot quicksort on already-sorted data).
 * Used in TravelSync to rank destinations by user-preference score.
 *
 * Expected time complexity: O(n log n)   Worst case: O(n^2) (astronomically
 * unlikely with random pivots).   Space complexity: O(log n) expected stack depth.
 */
public final class RandomizedQuickSort {

    private RandomizedQuickSort() { }

    public static <T> void sort(List<T> list, Comparator<T> comparator) {
        sort(list, comparator, new Random());
    }

    /** Overload accepting a seeded Random, for reproducible tests. */
    public static <T> void sort(List<T> list, Comparator<T> comparator, Random random) {
        quicksort(list, 0, list.size() - 1, comparator, random);
    }

    private static <T> void quicksort(List<T> list, int lo, int hi, Comparator<T> cmp, Random random) {
        if (lo >= hi) return;
        int pivotIndex = lo + random.nextInt(hi - lo + 1);
        java.util.Collections.swap(list, pivotIndex, hi);
        T pivot = list.get(hi);

        int i = lo;
        for (int j = lo; j < hi; j++) {
            if (cmp.compare(list.get(j), pivot) < 0) {
                java.util.Collections.swap(list, i, j);
                i++;
            }
        }
        java.util.Collections.swap(list, i, hi);

        quicksort(list, lo, i - 1, cmp, random);
        quicksort(list, i + 1, hi, cmp, random);
    }

    /**
     * One recorded moment of a traced sort.
     *
     * @param data        copy of the array at this moment
     * @param lo          start of the range being partitioned
     * @param hi          end (inclusive) of the range being partitioned
     * @param pivotIndex  current index of the pivot, or -1
     * @param scanIndex   index just compared with the pivot, or -1
     * @param boundary    end (exclusive) of the block already moved in front of the pivot
     * @param fixed       positions already in their final place
     * @param label       explanation of the step
     * @param comparisons comparisons so far
     */
    public record Frame(int[] data, int lo, int hi, int pivotIndex, int scanIndex, int boundary, boolean[] fixed,
                        String label, long comparisons) { }

    /** Runs the same algorithm on an int array and records every pivot choice, comparison and swap. */
    public static List<Frame> trace(int[] input, boolean descending, Random random) {
        int[] a = input.clone();
        List<Frame> frames = new ArrayList<>();
        boolean[] fixed = new boolean[a.length];
        long[] comparisons = {0};
        frames.add(new Frame(a.clone(), 0, a.length - 1, -1, -1, 0, fixed.clone(), "Unsorted input.", 0));
        traceSort(a, 0, a.length - 1, descending, random, frames, fixed, comparisons);
        Arrays.fill(fixed, true);
        frames.add(new Frame(a.clone(), 0, a.length - 1, -1, -1, 0, fixed.clone(),
                "Sorted " + (descending ? "from best to worst" : "ascending") + ".", comparisons[0]));
        return frames;
    }

    private static void traceSort(int[] a, int lo, int hi, boolean desc, Random random, List<Frame> frames,
                                  boolean[] fixed, long[] cmp) {
        if (lo > hi) return;
        if (lo == hi) {
            fixed[lo] = true;
            return;
        }
        int pivotIndex = lo + random.nextInt(hi - lo + 1);
        int chosenValue = a[pivotIndex];
        swap(a, pivotIndex, hi);
        frames.add(new Frame(a.clone(), lo, hi, hi, -1, lo, fixed.clone(),
                "Random pivot " + chosenValue + " (index " + pivotIndex + ") moved to the end of range [" + lo + ", " + hi + "].", cmp[0]));
        int pivot = a[hi];
        int i = lo;
        for (int j = lo; j < hi; j++) {
            cmp[0]++;
            boolean before = desc ? a[j] > pivot : a[j] < pivot;
            if (before) {
                swap(a, i, j);
                i++;
            }
            frames.add(new Frame(a.clone(), lo, hi, hi, j, i, fixed.clone(), "Compare " + (before ? a[i - 1] : a[j]) + " with pivot " + pivot
                    + (before ? ": it belongs in front, so it joins the front block." : ": it belongs behind the pivot, so it stays."), cmp[0]));
        }
        swap(a, i, hi);
        fixed[i] = true;
        frames.add(new Frame(a.clone(), lo, hi, i, -1, i, fixed.clone(), "Pivot " + pivot + " is placed at index " + i
                + ", its final position. Recurse on both sides.", cmp[0]));
        traceSort(a, lo, i - 1, desc, random, frames, fixed, cmp);
        traceSort(a, i + 1, hi, desc, random, frames, fixed, cmp);
    }

    private static void swap(int[] a, int i, int j) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }
}
