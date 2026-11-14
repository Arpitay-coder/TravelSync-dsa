package com.travelsync.algorithms.dp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OptimalBSTBuilderTest {

    @Test
    void computesKnownExpectedCost() {
        // Classic 3-key example: keys A,B,C with freq 3,4,2 -> optimal expected cost is well known
        String[] keys = {"A", "B", "C"};
        double[] freq = {3, 4, 2};
        var result = OptimalBSTBuilder.compute(keys, freq);
        // Optimal tree roots at B (freq 4): cost = freq(B)*1 + freq(A)*2 + freq(C)*2 = 4+6+4 = 14
        assertEquals(14.0, result.minExpectedCost(), 1e-9);
    }

    @Test
    void singleKeyCostEqualsItsFrequency() {
        var result = OptimalBSTBuilder.compute(new String[]{"X"}, new double[]{7});
        assertEquals(7.0, result.minExpectedCost(), 1e-9);
    }

    @Test
    void emptyInputHasZeroCost() {
        var result = OptimalBSTBuilder.compute(new String[0], new double[0]);
        assertEquals(0, result.minExpectedCost());
    }

    @Test
    void treeDescriptionIncludesAllKeys() {
        String[] keys = {"Bali", "Kyoto", "Rome", "Paris"};
        double[] freq = {5, 10, 2, 8};
        var result = OptimalBSTBuilder.compute(keys, freq);
        String description = OptimalBSTBuilder.describeTree(keys, result.rootTable(), 0, keys.length - 1, 0);
        for (String key : keys) {
            assertTrue(description.contains(key));
        }
    }
}
