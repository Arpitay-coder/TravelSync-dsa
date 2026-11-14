package com.travelsync.algorithms.dp;

import java.util.ArrayList;
import java.util.List;

/**
 * Levenshtein (edit) distance via classic O(n*m) dynamic programming.
 * Used in TravelSync to suggest corrections for misspelled destination/hotel
 * names entered by travelers.
 *
 * Time complexity: O(n*m)   Space complexity: O(n*m) (table kept for visualization).
 */
public final class LevenshteinCalculator {

    public record Result(int distance, int[][] table, List<String> operations) { }

    private LevenshteinCalculator() { }

    public static Result compute(String a, String b) {
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }

        List<String> ops = new ArrayList<>();
        int i = n, j = m;
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && a.charAt(i - 1) == b.charAt(j - 1) && dp[i][j] == dp[i - 1][j - 1]) {
                i--; j--;
            } else if (i > 0 && j > 0 && dp[i][j] == dp[i - 1][j - 1] + 1) {
                ops.add(0, "Substitute '" + a.charAt(i - 1) + "' -> '" + b.charAt(j - 1) + "' at position " + (i - 1));
                i--; j--;
            } else if (i > 0 && dp[i][j] == dp[i - 1][j] + 1) {
                ops.add(0, "Delete '" + a.charAt(i - 1) + "' at position " + (i - 1));
                i--;
            } else {
                ops.add(0, "Insert '" + b.charAt(j - 1) + "' at position " + i);
                j--;
            }
        }

        return new Result(dp[n][m], dp, ops);
    }
}
