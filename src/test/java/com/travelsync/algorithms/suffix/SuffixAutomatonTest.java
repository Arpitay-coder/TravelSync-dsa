package com.travelsync.algorithms.suffix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SuffixAutomatonTest {

    @Test
    void detectsExistingSubstrings() {
        SuffixAutomaton sam = new SuffixAutomaton("banana");
        assertTrue(sam.containsSubstring("ana"));
        assertTrue(sam.containsSubstring("banana"));
        assertTrue(sam.containsSubstring("nan"));
        assertTrue(sam.containsSubstring(""));
    }

    @Test
    void rejectsNonExistingSubstrings() {
        SuffixAutomaton sam = new SuffixAutomaton("banana");
        assertFalse(sam.containsSubstring("xyz"));
        assertFalse(sam.containsSubstring("bananas"));
    }

    @Test
    void findsLongestCommonSubstring() {
        SuffixAutomaton sam = new SuffixAutomaton("KyotoTemplesAndGardens");
        String lcs = sam.longestCommonSubstring("VisitKyotoTemplesToday");
        assertEquals("KyotoTemples", lcs);
    }

    @Test
    void returnsEmptyWhenNoCommonSubstring() {
        SuffixAutomaton sam = new SuffixAutomaton("abcdef");
        assertEquals("", sam.longestCommonSubstring("xyz123"));
    }

    @Test
    void stateCountIsWithinLinearBound() {
        SuffixAutomaton sam = new SuffixAutomaton("banana");
        // A suffix automaton has at most 2n-1 states for n >= 2.
        assertTrue(sam.getStateCount() <= 2 * 6);
    }
}
