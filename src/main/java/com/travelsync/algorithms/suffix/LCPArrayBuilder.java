package com.travelsync.algorithms.suffix;

/**
 * Kasai's algorithm: builds the LCP (Longest Common Prefix) array from a
 * suffix array in linear time. lcp[i] = length of the longest common prefix
 * between sa[i-1] and sa[i] (lcp[0] is unused/0 by convention).
 *
 * Used in TravelSync to identify common descriptions/phrases shared between
 * destinations (document similarity, M2).
 *
 * Time complexity: O(n)   Space complexity: O(n)
 */
public final class LCPArrayBuilder {

    private LCPArrayBuilder() { }

    public static int[] build(String s, int[] sa) {
        int n = s.length();
        int[] lcp = new int[n];
        if (n == 0) return lcp;

        int[] rank = new int[n];
        for (int i = 0; i < n; i++) rank[sa[i]] = i;

        int h = 0;
        for (int i = 0; i < n; i++) {
            if (rank[i] > 0) {
                int j = sa[rank[i] - 1];
                while (i + h < n && j + h < n && s.charAt(i + h) == s.charAt(j + h)) {
                    h++;
                }
                lcp[rank[i]] = h;
                if (h > 0) h--;
            } else {
                h = 0;
            }
        }
        return lcp;
    }

    /** Returns the longest repeated substring in s, using the LCP array's maximum value. */
    public static String longestRepeatedSubstring(String s) {
        if (s.isEmpty()) return "";
        int[] sa = SuffixArrayBuilder.build(s);
        int[] lcp = build(s, sa);
        int maxLen = 0, maxIdx = 0;
        for (int i = 1; i < lcp.length; i++) {
            if (lcp[i] > maxLen) {
                maxLen = lcp[i];
                maxIdx = i;
            }
        }
        return maxLen == 0 ? "" : s.substring(sa[maxIdx], sa[maxIdx] + maxLen);
    }
}
