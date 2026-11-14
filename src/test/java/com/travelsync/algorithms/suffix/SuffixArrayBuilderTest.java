package com.travelsync.algorithms.suffix;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class SuffixArrayBuilderTest {

    @Test
    void buildsCorrectSuffixArrayForKnownString() {
        // "banana" suffixes sorted: a(5) ana(3) anana(1) banana(0) na(4) nana(2)
        int[] sa = SuffixArrayBuilder.build("banana");
        assertArrayEquals(new int[]{5, 3, 1, 0, 4, 2}, sa);
    }

    @Test
    void handlesSingleCharacter() {
        assertArrayEquals(new int[]{0}, SuffixArrayBuilder.build("a"));
    }

    @Test
    void handlesEmptyString() {
        assertEquals(0, SuffixArrayBuilder.build("").length);
    }

    @Test
    void handlesAllIdenticalCharacters() {
        int[] sa = SuffixArrayBuilder.build("aaaa");
        assertArrayEquals(new int[]{3, 2, 1, 0}, sa);
    }

    @Test
    void suffixArrayIsAPermutationOfIndices() {
        int[] sa = SuffixArrayBuilder.build("SantoriniIslandGreece");
        int[] sorted = sa.clone();
        Arrays.sort(sorted);
        for (int i = 0; i < sorted.length; i++) assertEquals(i, sorted[i]);
    }
}
