package com.travelsync.algorithms.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Knuth-Morris-Pratt exact string matching.
 * Used in TravelSync to search destination/hotel names and travel keywords.
 *
 * Time complexity: O(n + m)   Space complexity: O(m)
 * where n = text length, m = pattern length.
 */
public final class KMPMatcher {

    private KMPMatcher() { }

    /** Builds the LPS (Longest Proper Prefix which is also Suffix) failure table. */
    public static int[] buildLpsTable(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0;
        int i = 1;
        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else if (len != 0) {
                len = lps[len - 1];
            } else {
                lps[i] = 0;
                i++;
            }
        }
        return lps;
    }

    /** Returns all starting indices in text where pattern occurs. */
    public static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        if (pattern.isEmpty() || text.isEmpty() || pattern.length() > text.length()) return matches;

        int[] lps = buildLpsTable(pattern);
        int n = text.length(), m = pattern.length();
        int i = 0, j = 0;
        while (i < n) {
            if (text.charAt(i) == pattern.charAt(j)) {
                i++; j++;
                if (j == m) {
                    matches.add(i - j);
                    j = lps[j - 1];
                }
            } else if (j != 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }
        return matches;
    }

    /** Full step-by-step trace (table build + search) for the UI visualizer. */
    public static List<StringMatchStep> trace(String text, String pattern) {
        List<StringMatchStep> steps = new ArrayList<>();
        if (pattern.isEmpty() || text.isEmpty()) return steps;

        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0, i = 1;
        steps.add(new StringMatchStep(StringMatchStep.Type.INFO, -1, -1,
                "Building LPS (failure) table for pattern \"" + pattern + "\""));
        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                steps.add(new StringMatchStep(StringMatchStep.Type.TABLE_BUILD, -1, i,
                        "lps[" + i + "] = " + len, Arrays.copyOf(lps, m)));
                i++;
            } else if (len != 0) {
                len = lps[len - 1];
                steps.add(new StringMatchStep(StringMatchStep.Type.TABLE_BUILD, -1, i,
                        "Fallback: len = lps[" + (len) + "]", Arrays.copyOf(lps, m)));
            } else {
                lps[i] = 0;
                steps.add(new StringMatchStep(StringMatchStep.Type.TABLE_BUILD, -1, i,
                        "lps[" + i + "] = 0", Arrays.copyOf(lps, m)));
                i++;
            }
        }

        steps.add(new StringMatchStep(StringMatchStep.Type.INFO, -1, -1, "Starting search phase"));
        int n = text.length();
        int ti = 0, pj = 0;
        while (ti < n) {
            if (text.charAt(ti) == pattern.charAt(pj)) {
                steps.add(new StringMatchStep(StringMatchStep.Type.COMPARE, ti, pj,
                        "Match char '" + text.charAt(ti) + "' at text[" + ti + "] == pattern[" + pj + "]"));
                ti++; pj++;
                if (pj == m) {
                    steps.add(new StringMatchStep(StringMatchStep.Type.FULL_MATCH, ti - pj, pj - 1,
                            "Full match found at index " + (ti - pj)));
                    pj = lps[pj - 1];
                }
            } else if (pj != 0) {
                steps.add(new StringMatchStep(StringMatchStep.Type.MISMATCH, ti, pj,
                        "Mismatch, fall back pattern index to lps[" + (pj - 1) + "] = " + lps[pj - 1]));
                pj = lps[pj - 1];
            } else {
                steps.add(new StringMatchStep(StringMatchStep.Type.MISMATCH, ti, pj,
                        "Mismatch at start of pattern, advance text index"));
                ti++;
            }
        }
        return steps;
    }
}
