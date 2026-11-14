package com.travelsync.algorithms.dp;

/**
 * Optimal Binary Search Tree: given a sorted set of keys (e.g. destination
 * names) and their search frequencies, builds the BST layout that minimizes
 * expected search cost. Used in TravelSync to arrange frequently-searched
 * destinations/services for fastest average lookup.
 *
 * Time complexity: O(n^3)   Space complexity: O(n^2).
 */
public final class OptimalBSTBuilder {

    public record Result(double minExpectedCost, int[][] rootTable) { }

    private OptimalBSTBuilder() { }

    /** keys.length == freq.length == n. Frequencies need not be normalized. */
    public static Result compute(String[] keys, double[] freq) {
        int n = keys.length;
        if (n == 0) return new Result(0, new int[0][0]);

        double[][] cost = new double[n + 1][n + 1];
        double[][] freqSum = new double[n + 1][n + 1];
        int[][] root = new int[n][n];

        for (int i = 0; i < n; i++) {
            cost[i][i] = freq[i];
            freqSum[i][i] = freq[i];
            root[i][i] = i;
        }

        for (int len = 2; len <= n; len++) {
            for (int i = 0; i <= n - len; i++) {
                int j = i + len - 1;
                cost[i][j] = Double.POSITIVE_INFINITY;
                freqSum[i][j] = freqSum[i][j - 1] + freq[j];

                for (int r = i; r <= j; r++) {
                    double left = (r > i) ? cost[i][r - 1] : 0;
                    double right = (r < j) ? cost[r + 1][j] : 0;
                    double total = left + right + freqSum[i][j];
                    if (total < cost[i][j]) {
                        cost[i][j] = total;
                        root[i][j] = r;
                    }
                }
            }
        }

        return new Result(cost[0][n - 1], root);
    }

    /** Renders the tree structure as an indented text outline for display. */
    public static String describeTree(String[] keys, int[][] root, int i, int j, int depth) {
        if (i > j) return "";
        int r = root[i][j];
        StringBuilder sb = new StringBuilder();
        sb.append("  ".repeat(depth)).append(keys[r]).append("\n");
        sb.append(describeTree(keys, root, i, r - 1, depth + 1));
        sb.append(describeTree(keys, root, r + 1, j, depth + 1));
        return sb.toString();
    }
}
