package com.travelsync.algorithms.parallel;

/**
 * Brent's theorem: for a computation with total work W (sum of all operation
 * costs) and depth D (length of the longest dependency chain), a schedule on
 * p processors completes in time T_p such that
 *
 *     max(D, W/p)  <=  T_p  <=  W/p + D
 *
 * Used in TravelSync to evaluate whether the parallel algorithms in this
 * module (Blelloch Scan, Parallel Reduce) are using available processors
 * efficiently, by comparing their measured running time against this bound.
 */
public final class BrentsTheoremEvaluator {

    public record Bound(double lowerBound, double upperBound) { }

    private BrentsTheoremEvaluator() { }

    public static Bound computeBound(long workUnits, long depth, int processors) {
        double lower = Math.max(depth, workUnits / (double) processors);
        double upper = (workUnits / (double) processors) + depth;
        return new Bound(lower, upper);
    }

    /** Ideal speedup achievable with p processors given the work/depth (Brent's upper bound on speedup). */
    public static double idealSpeedup(long workUnits, long depth, int processors) {
        Bound bound = computeBound(workUnits, depth, processors);
        return workUnits / bound.upperBound();
    }
}
