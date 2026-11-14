package com.travelsync.algorithms.string;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ZFunctionMatcherTest {

    @Test
    void computesKnownZArray() {
        // classic example: "aaabaab"
        int[] z = ZFunctionMatcher.computeZArray("aaabaab");
        assertEquals(7, z.length);
        assertEquals(7, z[0]);
    }

    @Test
    void findsAllOccurrencesOfRepeatedPhrase() {
        List<Integer> matches = ZFunctionMatcher.search("thebeachnearthebeachresort", "thebeach");
        assertEquals(List.of(0, 12), matches);
    }

    @Test
    void noMatchReturnsEmptyList() {
        assertTrue(ZFunctionMatcher.search("mountainretreat", "ocean").isEmpty());
    }

    @Test
    void handlesEmptyInputsGracefully() {
        assertTrue(ZFunctionMatcher.search("", "abc").isEmpty());
        assertTrue(ZFunctionMatcher.search("abc", "").isEmpty());
    }

    @Test
    void singleCharacterPattern() {
        List<Integer> matches = ZFunctionMatcher.search("banana", "a");
        assertEquals(List.of(1, 3, 5), matches);
    }
}
