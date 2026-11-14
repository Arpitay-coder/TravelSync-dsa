package com.travelsync.algorithms.suffix;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SuffixAutomatonWalkTest {

    @Test
    void walkLengthsReachTheLongestCommonSubstringLength() {
        SuffixAutomaton sam = new SuffixAutomaton("KyotoTemplesAndGardens");
        String other = "VisitKyotoTemplesToday";
        List<SuffixAutomaton.WalkStep> steps = sam.walk(other);
        int best = steps.stream().mapToInt(SuffixAutomaton.WalkStep::length).max().orElse(0);
        assertEquals(sam.longestCommonSubstring(other).length(), best);
        assertEquals(other.length(), steps.size());
    }

    @Test
    void walkOfDisjointAlphabetNeverMatches() {
        List<SuffixAutomaton.WalkStep> steps = new SuffixAutomaton("abc").walk("xyz");
        assertTrue(steps.stream().allMatch(s -> s.length() == 0));
    }

    @Test
    void walkReportsFallbackWhenTransitionIsMissing() {
        // after reading "ab" the automaton of "abab" has no 'x' transition, so reading 'x' must fall back
        List<SuffixAutomaton.WalkStep> steps = new SuffixAutomaton("abab").walk("abx");
        assertTrue(steps.get(2).fellBack());
        assertEquals(0, steps.get(2).length());
    }
}
