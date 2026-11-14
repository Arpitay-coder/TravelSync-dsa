package com.travelsync.algorithms.string;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RabinKarpMatcherTest {

    @Test
    void findsBookingCodeOccurrence() {
        List<Integer> matches = RabinKarpMatcher.search("REF-BK-7F2A19-CONFIRMED", "BK-7F2A19");
        assertEquals(List.of(4), matches);
    }

    @Test
    void findsMultipleOccurrences() {
        List<Integer> matches = RabinKarpMatcher.search("abcabcabc", "abc");
        assertEquals(List.of(0, 3, 6), matches);
    }

    @Test
    void returnsEmptyWhenPatternAbsent() {
        assertTrue(RabinKarpMatcher.search("BK-1111-CONFIRMED", "BK-2222").isEmpty());
    }

    @Test
    void handlesPatternEqualToText() {
        assertEquals(List.of(0), RabinKarpMatcher.search("EXACT", "EXACT"));
    }

    @Test
    void handlesHashCollisionCandidatesCorrectly() {
        // Same-length strings that could collide should still be verified by char match
        List<Integer> matches = RabinKarpMatcher.search("xyxyxzxyxz", "xyxz");
        assertEquals(List.of(2, 6), matches);
    }

    @Test
    void handlesEmptyPattern() {
        assertTrue(RabinKarpMatcher.search("text", "").isEmpty());
    }
}
