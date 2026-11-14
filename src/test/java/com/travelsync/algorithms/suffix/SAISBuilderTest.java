package com.travelsync.algorithms.suffix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SAISBuilderTest {

    private void assertMatchesDoublingMethod(String s) {
        int[] expected = SuffixArrayBuilder.build(s);
        int[] actual = SAISBuilder.build(s);
        assertArrayEquals(expected, actual, "SA-IS disagreed with doubling method for input: " + s);
    }

    @Test
    void matchesDoublingMethodForBanana() {
        assertMatchesDoublingMethod("banana");
    }

    @Test
    void matchesDoublingMethodForRepeatedCharacters() {
        assertMatchesDoublingMethod("aaaaaaaa");
    }

    @Test
    void matchesDoublingMethodForMixedCaseTravelText() {
        assertMatchesDoublingMethod("SantoriniIslandGreeceSantoriniSunset");
    }

    @Test
    void matchesDoublingMethodForSingleCharacter() {
        assertMatchesDoublingMethod("x");
    }

    @Test
    void matchesDoublingMethodForPalindrome() {
        assertMatchesDoublingMethod("abacabadabacaba");
    }

    @Test
    void handlesEmptyString() {
        assertEquals(0, SAISBuilder.build("").length);
    }

    @Test
    void matchesDoublingMethodForLongerRealisticText() {
        assertMatchesDoublingMethod("Kyoto temples Kyoto gardens Kyoto geisha districts Kyoto shrines");
    }
}
