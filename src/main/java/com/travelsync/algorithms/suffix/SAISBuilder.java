package com.travelsync.algorithms.suffix;

import java.util.Arrays;
import java.util.TreeSet;

/**
 * SA-IS (Suffix Array by Induced Sorting) -- Nong, Zhang &amp; Chen's linear-time
 * suffix array construction algorithm. Used in TravelSync for large-scale
 * travel-document indexing where near-linear construction matters.
 *
 * Time complexity: O(n)   Space complexity: O(n)
 *
 * This is independently cross-checked in tests against the doubling-based
 * {@link SuffixArrayBuilder}: both must produce identical suffix arrays for
 * any input, which is a strong correctness signal for two unrelated
 * algorithms computing the same object.
 */
public final class SAISBuilder {

    private SAISBuilder() { }

    /** Public entry point: builds the suffix array of s using SA-IS. */
    public static int[] build(String s) {
        int n = s.length();
        if (n == 0) return new int[0];

        TreeSet<Character> distinctChars = new TreeSet<>();
        for (char c : s.toCharArray()) distinctChars.add(c);
        int[] charRank = new int[Character.MAX_VALUE + 1];
        int rank = 1;
        for (char c : distinctChars) charRank[c] = rank++;

        int[] input = new int[n + 1];
        for (int i = 0; i < n; i++) input[i] = charRank[s.charAt(i)];
        input[n] = 0; // sentinel: smallest, unique

        int alphabetSize = distinctChars.size() + 1; // + sentinel
        int[] sa = new int[n + 1];
        sais(input, sa, n + 1, alphabetSize);

        // sa[0] is always the sentinel's own (empty) suffix -- drop it.
        int[] result = new int[n];
        System.arraycopy(sa, 1, result, 0, n);
        return result;
    }

    private static boolean isLMS(boolean[] t, int i) {
        return i > 0 && t[i] && !t[i - 1];
    }

    private static int[] getBucketSizes(int[] s, int n, int K) {
        int[] bkt = new int[K];
        for (int i = 0; i < n; i++) bkt[s[i]]++;
        return bkt;
    }

    private static int[] getBuckets(int[] bktSizes, int K, boolean end) {
        int[] bkt = new int[K];
        int sum = 0;
        for (int i = 0; i < K; i++) {
            sum += bktSizes[i];
            bkt[i] = end ? sum : sum - bktSizes[i];
        }
        return bkt;
    }

    private static void induceSortL(int[] s, int[] sa, int[] bktSizes, boolean[] t, int n, int K) {
        int[] bkt = getBuckets(bktSizes, K, false);
        for (int i = 0; i < n; i++) {
            int j = sa[i] - 1;
            if (j >= 0 && !t[j]) {
                sa[bkt[s[j]]++] = j;
            }
        }
    }

    private static void induceSortS(int[] s, int[] sa, int[] bktSizes, boolean[] t, int n, int K) {
        int[] bkt = getBuckets(bktSizes, K, true);
        for (int i = n - 1; i >= 0; i--) {
            int j = sa[i] - 1;
            if (j >= 0 && t[j]) {
                sa[--bkt[s[j]]] = j;
            }
        }
    }

    private static void sais(int[] s, int[] sa, int n, int K) {
        boolean[] t = new boolean[n];
        t[n - 1] = true;
        for (int i = n - 2; i >= 0; i--) {
            t[i] = s[i] < s[i + 1] || (s[i] == s[i + 1] && t[i + 1]);
        }

        int[] bktSizes = getBucketSizes(s, n, K);

        Arrays.fill(sa, -1);
        int[] bkt = getBuckets(bktSizes, K, true);
        for (int i = 1; i < n; i++) {
            if (isLMS(t, i)) {
                sa[--bkt[s[i]]] = i;
            }
        }

        induceSortL(s, sa, bktSizes, t, n, K);
        induceSortS(s, sa, bktSizes, t, n, K);

        int n1 = 0;
        for (int i = 0; i < n; i++) {
            if (isLMS(t, sa[i])) {
                sa[n1++] = sa[i];
            }
        }
        for (int i = n1; i < n; i++) sa[i] = -1;

        int[] name = new int[n];
        Arrays.fill(name, -1);
        int nameCount = 0;
        int prev = -1;
        for (int i = 0; i < n1; i++) {
            int pos = sa[i];
            boolean diff = (prev == -1);
            if (!diff) {
                int d = 0;
                while (true) {
                    boolean posIsLMS = isLMS(t, pos + d);
                    boolean prevIsLMS = isLMS(t, prev + d);
                    if (d > 0 && (posIsLMS || prevIsLMS)) {
                        if (posIsLMS != prevIsLMS || s[pos + d] != s[prev + d] || t[pos + d] != t[prev + d]) {
                            diff = true;
                        }
                        break;
                    }
                    if (s[pos + d] != s[prev + d] || t[pos + d] != t[prev + d]) {
                        diff = true;
                        break;
                    }
                    d++;
                }
            }
            if (diff) {
                nameCount++;
                prev = pos;
            }
            name[pos] = nameCount - 1;
        }

        int[] s1 = new int[n1];
        int j = 0;
        for (int i = 0; i < n; i++) {
            if (name[i] >= 0) s1[j++] = name[i];
        }

        int[] sa1 = new int[n1];
        if (nameCount < n1) {
            sais(s1, sa1, n1, nameCount);
        } else {
            for (int i = 0; i < n1; i++) sa1[s1[i]] = i;
        }

        int[] lmsPos = new int[n1];
        j = 0;
        for (int i = 1; i < n; i++) {
            if (isLMS(t, i)) lmsPos[j++] = i;
        }
        for (int i = 0; i < n1; i++) sa1[i] = lmsPos[sa1[i]];

        Arrays.fill(sa, -1);
        bkt = getBuckets(bktSizes, K, true);
        for (int i = n1 - 1; i >= 0; i--) {
            j = sa1[i];
            sa[--bkt[s[j]]] = j;
        }
        induceSortL(s, sa, bktSizes, t, n, K);
        induceSortS(s, sa, bktSizes, t, n, K);
    }
}
