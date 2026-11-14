package com.travelsync.algorithms.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.BinaryOperator;

/**
 * Parallel reduction using Java's Fork/Join framework: genuinely splits the
 * input across the common ForkJoinPool's worker threads once a chunk drops
 * below a threshold, rather than simulating parallelism on one thread.
 * Used in TravelSync to aggregate travel-demand metrics (e.g. total
 * projected bookings across all destinations) across available cores.
 *
 * Work complexity: O(n)   Depth (span): O(log n) with enough processors.
 */
public final class ParallelReducer {

    private static final int THRESHOLD = 1000;

    private ParallelReducer() { }

    public static long reduceSum(long[] data) {
        return ForkJoinPool.commonPool().invoke(new SumTask(data, 0, data.length));
    }

    /** Sums on a dedicated pool with exactly {@code parallelism} worker threads (used to compare processor counts). */
    public static long reduceSum(long[] data, int parallelism) {
        ForkJoinPool pool = new ForkJoinPool(Math.max(1, parallelism));
        try {
            return pool.invoke(new SumTask(data, 0, data.length));
        } finally {
            pool.shutdown();
        }
    }

    /**
     * The levels of the balanced reduction tree: level 0 is the input, every further level adds neighbouring pairs
     * (an odd last element is carried up). All additions inside one level are independent, so a level is one parallel step.
     */
    public static List<long[]> reductionLevels(long[] data) {
        List<long[]> levels = new ArrayList<>();
        long[] current = data.clone();
        levels.add(current);
        while (current.length > 1) {
            long[] next = new long[(current.length + 1) / 2];
            for (int i = 0; i < next.length; i++) {
                next[i] = current[2 * i] + (2 * i + 1 < current.length ? current[2 * i + 1] : 0);
            }
            levels.add(next);
            current = next;
        }
        return levels;
    }

    public static long reduceCustom(long[] data, long identity, BinaryOperator<Long> op) {
        return ForkJoinPool.commonPool().invoke(new CustomTask(data, 0, data.length, identity, op));
    }

    private static class SumTask extends RecursiveTask<Long> {
        private final long[] data;
        private final int lo, hi;

        SumTask(long[] data, int lo, int hi) {
            this.data = data; this.lo = lo; this.hi = hi;
        }

        @Override
        protected Long compute() {
            if (hi - lo <= THRESHOLD) {
                long sum = 0;
                for (int i = lo; i < hi; i++) sum += data[i];
                return sum;
            }
            int mid = (lo + hi) / 2;
            SumTask left = new SumTask(data, lo, mid);
            SumTask right = new SumTask(data, mid, hi);
            left.fork();
            long rightResult = right.compute();
            long leftResult = left.join();
            return leftResult + rightResult;
        }
    }

    private static class CustomTask extends RecursiveTask<Long> {
        private final long[] data;
        private final int lo, hi;
        private final long identity;
        private final BinaryOperator<Long> op;

        CustomTask(long[] data, int lo, int hi, long identity, BinaryOperator<Long> op) {
            this.data = data; this.lo = lo; this.hi = hi; this.identity = identity; this.op = op;
        }

        @Override
        protected Long compute() {
            if (hi - lo <= THRESHOLD) {
                long acc = identity;
                for (int i = lo; i < hi; i++) acc = op.apply(acc, data[i]);
                return acc;
            }
            int mid = (lo + hi) / 2;
            CustomTask left = new CustomTask(data, lo, mid, identity, op);
            CustomTask right = new CustomTask(data, mid, hi, identity, op);
            left.fork();
            long rightResult = right.compute();
            long leftResult = left.join();
            return op.apply(leftResult, rightResult);
        }
    }
}
