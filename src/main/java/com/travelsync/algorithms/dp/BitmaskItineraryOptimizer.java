package com.travelsync.algorithms.dp;

import java.util.ArrayList;
import java.util.List;

/**
 * Bitmask DP (Held-Karp) to select the minimum-cost order of visiting a small
 * set of destinations starting from a fixed origin -- used in TravelSync to
 * plan an efficient multi-stop itinerary within a short trip.
 *
 * Time complexity: O(2^n * n^2)   Space complexity: O(2^n * n).
 * Practical only for small n (itineraries of a handful of stops), which
 * matches the "short itinerary" use case.
 */
public final class BitmaskItineraryOptimizer {

    public record Result(double totalCost, List<Integer> order) { }

    private BitmaskItineraryOptimizer() { }

    /**
     * @param dist  dist[i][j] = travel cost/time from stop i to stop j (stops indexed 0..n-1)
     * @param start index of the fixed starting stop
     * @return the minimum-cost order visiting every stop exactly once, starting at `start`
     */
    public static Result solve(double[][] dist, int start) {
        int n = dist.length;
        if (n == 0) return new Result(0, List.of());
        if (n == 1) return new Result(0, List.of(start));

        int fullMask = (1 << n) - 1;
        double[][] dp = new double[1 << n][n];
        int[][] parent = new int[1 << n][n];
        for (double[] row : dp) java.util.Arrays.fill(row, Double.POSITIVE_INFINITY);
        for (int[] row : parent) java.util.Arrays.fill(row, -1);

        dp[1 << start][start] = 0;

        for (int mask = 1; mask <= fullMask; mask++) {
            if ((mask & (1 << start)) == 0) continue;
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) == 0 || dp[mask][i] == Double.POSITIVE_INFINITY) continue;
                for (int j = 0; j < n; j++) {
                    if ((mask & (1 << j)) != 0) continue;
                    int nextMask = mask | (1 << j);
                    double candidate = dp[mask][i] + dist[i][j];
                    if (candidate < dp[nextMask][j]) {
                        dp[nextMask][j] = candidate;
                        parent[nextMask][j] = i;
                    }
                }
            }
        }

        double best = Double.POSITIVE_INFINITY;
        int bestEnd = start;
        for (int i = 0; i < n; i++) {
            if (dp[fullMask][i] < best) {
                best = dp[fullMask][i];
                bestEnd = i;
            }
        }

        List<Integer> order = new ArrayList<>();
        int mask = fullMask, cur = bestEnd;
        while (cur != -1) {
            order.add(0, cur);
            int prev = parent[mask][cur];
            mask ^= (1 << cur);
            cur = prev;
        }

        return new Result(best, order);
    }
}
