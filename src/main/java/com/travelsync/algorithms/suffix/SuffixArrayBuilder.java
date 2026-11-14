package com.travelsync.algorithms.suffix;

import java.util.Arrays;

/**
 * Suffix Array construction using the prefix-doubling (rank-doubling) method.
 * Used in TravelSync to index hotel/destination/travel descriptions for fast
 * substring search and comparison.
 *
 * Time complexity: O(n log n)   Space complexity: O(n)
 */
public final class SuffixArrayBuilder {

    private SuffixArrayBuilder() { }

    /** Builds the suffix array of s: sa[i] = starting index of the i-th smallest suffix. */
    public static int[] build(String s) {
        int n = s.length();
        Integer[] sa = new Integer[n];
        int[] rank = new int[n];
        int[] tmp = new int[n];

        for (int i = 0; i < n; i++) {
            sa[i] = i;
            rank[i] = s.charAt(i);
        }

        for (int k = 1; k < n; k <<= 1) {
            final int kk = k;
            final int[] r = rank;
            java.util.Comparator<Integer> cmp = (a, b) -> {
                if (r[a] != r[b]) return Integer.compare(r[a], r[b]);
                int ra = a + kk < n ? r[a + kk] : -1;
                int rb = b + kk < n ? r[b + kk] : -1;
                return Integer.compare(ra, rb);
            };
            Arrays.sort(sa, cmp);

            tmp[sa[0]] = 0;
            for (int i = 1; i < n; i++) {
                tmp[sa[i]] = tmp[sa[i - 1]] + (cmp.compare(sa[i - 1], sa[i]) < 0 ? 1 : 0);
            }
            rank = Arrays.copyOf(tmp, n);

            if (rank[sa[n - 1]] == n - 1) break; // fully ranked, no need to continue
        }

        int[] result = new int[n];
        for (int i = 0; i < n; i++) result[i] = sa[i];
        return result;
    }
}
