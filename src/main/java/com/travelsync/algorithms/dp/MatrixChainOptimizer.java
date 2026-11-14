package com.travelsync.algorithms.dp;

/**
 * Matrix Chain Multiplication: finds the parenthesization that minimizes the
 * number of scalar multiplications when multiplying a chain of matrices.
 * Used in TravelSync to optimize the order in which chained travel-data
 * transformations (e.g. successive filter/aggregate matrix operations over
 * booking or demand data) are applied.
 *
 * Time complexity: O(n^3)   Space complexity: O(n^2).
 */
public final class MatrixChainOptimizer {

    public record Result(long minMultiplications, int[][] splitTable, String parenthesization, long[][] costTable) { }

    private MatrixChainOptimizer() { }

    /** dims has length n+1 for n matrices, where matrix i has dimensions dims[i-1] x dims[i]. */
    public static Result compute(int[] dims) {
        int n = dims.length - 1;
        if (n <= 0) return new Result(0, new int[0][0], "", new long[0][0]);
        long[][] cost = new long[n + 1][n + 1];
        int[][] split = new int[n + 1][n + 1];

        for (int len = 2; len <= n; len++) {
            for (int i = 1; i <= n - len + 1; i++) {
                int j = i + len - 1;
                cost[i][j] = Long.MAX_VALUE;
                for (int k = i; k < j; k++) {
                    long candidate = cost[i][k] + cost[k + 1][j]
                            + (long) dims[i - 1] * dims[k] * dims[j];
                    if (candidate < cost[i][j]) {
                        cost[i][j] = candidate;
                        split[i][j] = k;
                    }
                }
            }
        }

        String paren = buildParenthesization(split, 1, n);
        return new Result(cost[1][n], split, paren, cost);
    }

    private static String buildParenthesization(int[][] split, int i, int j) {
        if (i == j) return "M" + i;
        int k = split[i][j];
        return "(" + buildParenthesization(split, i, k) + " x " + buildParenthesization(split, k + 1, j) + ")";
    }
}
