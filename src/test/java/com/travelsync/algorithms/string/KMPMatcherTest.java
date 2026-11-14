package com.travelsync.algorithms.string;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KMPMatcherTest {

    @Test
    void findsSingleOccurrence() {
        List<Integer> matches = KMPMatcher.search("SantoriniIslandGreece", "Island");
        assertEquals(List.of(9), matches);
    }

    @Test
    void findsMultipleOverlappingOccurrences() {
        List<Integer> matches = KMPMatcher.search("aaaaa", "aa");
        assertEquals(List.of(0, 1, 2, 3), matches);
    }

    @Test
    void returnsEmptyWhenNoMatch() {
        assertTrue(KMPMatcher.search("Bali Indonesia", "Paris").isEmpty());
    }

    @Test
    void handlesEmptyPatternGracefully() {
        assertTrue(KMPMatcher.search("Bali", "").isEmpty());
    }

    @Test
    void handlesPatternLongerThanText() {
        assertTrue(KMPMatcher.search("Goa", "GoaBeachResort").isEmpty());
    }

    @Test
    void lpsTableIsCorrectForKnownPattern() {
        // classic textbook example
        int[] lps = KMPMatcher.buildLpsTable("ababcababa");
        assertArrayEquals(new int[]{0, 0, 1, 2, 0, 1, 2, 3, 4, 3}, lps);
    }

    @Test
    void traceEndsWithFullMatchStep() {
        List<StringMatchStep> steps = KMPMatcher.trace("VisitKyotoJapan", "Kyoto");
        assertTrue(steps.stream().anyMatch(s -> s.getType() == StringMatchStep.Type.FULL_MATCH));
    }
}
