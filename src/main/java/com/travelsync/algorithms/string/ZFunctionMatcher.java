package com.travelsync.algorithms.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Z-Function string matching. Used in TravelSync to identify repeated phrases
 * within travel descriptions, and (combined with a separator) to search patterns.
 *
 * Time complexity: O(n)   Space complexity: O(n)
 */
public final class ZFunctionMatcher {

    private ZFunctionMatcher() { }

    /** Computes the Z-array where z[i] = length of the longest substring starting at i
     *  that matches a prefix of s. */
    public static int[] computeZArray(String s) {
        int n = s.length();
        int[] z = new int[n];
        if (n == 0) return z;
        z[0] = n;
        int l = 0, r = 0;
        for (int i = 1; i < n; i++) {
            if (i < r) {
                z[i] = Math.min(r - i, z[i - l]);
            }
            while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) {
                z[i]++;
            }
            if (i + z[i] > r) {
                l = i;
                r = i + z[i];
            }
        }
        return z;
    }

    /** Searches for pattern occurrences in text using pattern + separator + text. */
    public static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        if (pattern.isEmpty() || text.isEmpty()) return matches;
        String combined = pattern + "\u0001" + text;
        int[] z = computeZArray(combined);
        int m = pattern.length();
        for (int i = m + 1; i < combined.length(); i++) {
            if (z[i] >= m) {
                matches.add(i - m - 1);
            }
        }
        return matches;
    }

    /** Full trace of Z-array construction over pattern+separator+text, for the visualizer. */
    public static List<StringMatchStep> trace(String text, String pattern) {
        List<StringMatchStep> steps = new ArrayList<>();
        if (pattern.isEmpty() || text.isEmpty()) return steps;
        String combined = pattern + "\u0001" + text;
        int n = combined.length();
        int[] z = new int[n];
        z[0] = n;
        steps.add(new StringMatchStep(StringMatchStep.Type.INFO, -1, -1,
                "Combined string = pattern + separator + text, length " + n));
        int l = 0, r = 0;
        int m = pattern.length();
        for (int i = 1; i < n; i++) {
            if (i < r) {
                z[i] = Math.min(r - i, z[i - l]);
                steps.add(new StringMatchStep(StringMatchStep.Type.COMPARE, i, -1,
                        "Using box [l=" + l + ", r=" + r + "]: z[" + i + "] initialised to " + z[i],
                        Arrays.copyOf(z, n)));
            }
            while (i + z[i] < n && combined.charAt(z[i]) == combined.charAt(i + z[i])) {
                z[i]++;
            }
            steps.add(new StringMatchStep(StringMatchStep.Type.TABLE_BUILD, i, -1,
                    "z[" + i + "] = " + z[i], Arrays.copyOf(z, n)));
            if (i + z[i] > r) {
                l = i;
                r = i + z[i];
            }
            if (i > m && z[i] >= m) {
                steps.add(new StringMatchStep(StringMatchStep.Type.FULL_MATCH, i - m - 1, -1,
                        "Match found in text at index " + (i - m - 1)));
            }
        }
        return steps;
    }
}
