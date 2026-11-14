package com.travelsync.algorithms.dp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DamerauLevenshteinCalculatorTest {

    @Test
    void treatsAdjacentTranspositionAsSingleEdit() {
        // "Barcleona" vs "Barcelona": 'le' transposed to 'el' -> single transposition
        assertEquals(1, DamerauLevenshteinCalculator.compute("Barcleona", "Barcelona").distance());
    }

    @Test
    void plainLevenshteinCasesStillWork() {
        assertEquals(3, DamerauLevenshteinCalculator.compute("kitten", "sitting").distance());
    }

    @Test
    void identicalStringsHaveZeroDistance() {
        assertEquals(0, DamerauLevenshteinCalculator.compute("Rome", "Rome").distance());
    }

    @Test
    void transpositionDistanceIsLessThanOrEqualToPlainLevenshtein() {
        int damerau = DamerauLevenshteinCalculator.compute("Barcleona", "Barcelona").distance();
        int plain = LevenshteinCalculator.compute("Barcleona", "Barcelona").distance();
        assertTrue(damerau <= plain);
    }
}
