package com.travelsync.algorithms.dp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LevenshteinCalculatorTest {

    @Test
    void computesKnownDistance() {
        assertEquals(3, LevenshteinCalculator.compute("kitten", "sitting").distance());
    }

    @Test
    void identicalStringsHaveZeroDistance() {
        assertEquals(0, LevenshteinCalculator.compute("Paris", "Paris").distance());
    }

    @Test
    void emptyStringDistanceEqualsOtherLength() {
        assertEquals(5, LevenshteinCalculator.compute("", "Tokyo").distance());
        assertEquals(5, LevenshteinCalculator.compute("Tokyo", "").distance());
    }

    @Test
    void correctsCommonMisspelling() {
        assertEquals(1, LevenshteinCalculator.compute("Bangkok", "Bankok").distance());
    }

    @Test
    void operationsListIsNonEmptyWhenStringsDiffer() {
        var result = LevenshteinCalculator.compute("cat", "cut");
        assertFalse(result.operations().isEmpty());
    }
}
