package com.travelsync.algorithms.suffix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LCPArrayBuilderTest {

    @Test
    void computesKnownLcpArrayForBanana() {
        // sa = [5,3,1,0,4,2] -> suffixes: a, ana, anana, banana, na, nana
        int[] sa = SuffixArrayBuilder.build("banana");
        int[] lcp = LCPArrayBuilder.build("banana", sa);
        // lcp[0] unused(0); a/ana=1; ana/anana=3; anana/banana=0; banana/na=0; na/nana=2
        assertArrayEquals(new int[]{0, 1, 3, 0, 0, 2}, lcp);
    }

    @Test
    void findsLongestRepeatedSubstring() {
        assertEquals("anana", LCPArrayBuilder.longestRepeatedSubstring("banana"));
    }

    @Test
    void returnsEmptyWhenNoRepeats() {
        assertEquals("", LCPArrayBuilder.longestRepeatedSubstring("abcdef"));
    }

    @Test
    void handlesEmptyString() {
        assertEquals(0, LCPArrayBuilder.build("", new int[0]).length);
    }
}
