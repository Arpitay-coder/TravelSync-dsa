package com.travelsync.algorithms.string;

import java.util.ArrayList;
import java.util.List;

/**
 * Rabin-Karp exact string matching using a polynomial rolling hash.
 * Used in TravelSync to search booking codes and reservation IDs.
 *
 * Average/expected time complexity: O(n + m).
 * Worst case (many hash collisions): O(n * m).   Space complexity: O(1).
 */
public final class RabinKarpMatcher {

    private static final long BASE = 256L;
    private static final long MOD = 1_000_000_007L;

    private RabinKarpMatcher() { }

    public static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        int n = text.length(), m = pattern.length();
        if (m == 0 || n == 0 || m > n) return matches;

        long patternHash = 0, windowHash = 0, highOrder = 1;
        for (int i = 0; i < m - 1; i++) highOrder = (highOrder * BASE) % MOD;

        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * BASE + pattern.charAt(i)) % MOD;
            windowHash = (windowHash * BASE + text.charAt(i)) % MOD;
        }

        for (int i = 0; i <= n - m; i++) {
            if (patternHash == windowHash && text.regionMatches(i, pattern, 0, m)) {
                matches.add(i);
            }
            if (i < n - m) {
                windowHash = (BASE * (windowHash - text.charAt(i) * highOrder % MOD + MOD) + text.charAt(i + m)) % MOD;
            }
        }
        return matches;
    }

    /** Full trace including hash values at every window, and collision checks. */
    public static List<StringMatchStep> trace(String text, String pattern) {
        List<StringMatchStep> steps = new ArrayList<>();
        int n = text.length(), m = pattern.length();
        if (m == 0 || n == 0 || m > n) return steps;

        long patternHash = 0, windowHash = 0, highOrder = 1;
        for (int i = 0; i < m - 1; i++) highOrder = (highOrder * BASE) % MOD;

        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * BASE + pattern.charAt(i)) % MOD;
            windowHash = (windowHash * BASE + text.charAt(i)) % MOD;
        }
        steps.add(new StringMatchStep(StringMatchStep.Type.INFO, 0, -1,
                "Pattern hash computed = " + patternHash));

        for (int i = 0; i <= n - m; i++) {
            steps.add(new StringMatchStep(StringMatchStep.Type.COMPARE, i, -1,
                    "Window [" + i + ", " + (i + m - 1) + "] hash = " + windowHash
                            + (windowHash == patternHash ? " (matches pattern hash, verifying chars)" : "")));
            if (patternHash == windowHash) {
                if (text.regionMatches(i, pattern, 0, m)) {
                    steps.add(new StringMatchStep(StringMatchStep.Type.FULL_MATCH, i, -1,
                            "Verified true match at index " + i));
                } else {
                    steps.add(new StringMatchStep(StringMatchStep.Type.MISMATCH, i, -1,
                            "Hash collision - character check failed, not a real match"));
                }
            }
            if (i < n - m) {
                windowHash = (BASE * (windowHash - text.charAt(i) * highOrder % MOD + MOD) + text.charAt(i + m)) % MOD;
            }
        }
        return steps;
    }
}
