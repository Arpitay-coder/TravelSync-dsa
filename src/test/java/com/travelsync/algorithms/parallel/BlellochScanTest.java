package com.travelsync.algorithms.parallel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlellochScanTest {

    @Test
    void computesKnownExclusiveScanForPowerOfTwoLength() {
        long[] input = {1, 2, 3, 4};
        long[] result = BlellochScan.exclusiveScan(input);
        assertArrayEquals(new long[]{0, 1, 3, 6}, result);
    }

    @Test
    void computesExclusiveScanForNonPowerOfTwoLength() {
        long[] input = {5, 3, 7, 2, 9};
        long[] result = BlellochScan.exclusiveScan(input);
        assertArrayEquals(new long[]{0, 5, 8, 15, 17}, result);
    }

    @Test
    void handlesSingleElement() {
        long[] result = BlellochScan.exclusiveScan(new long[]{42});
        assertArrayEquals(new long[]{0}, result);
    }

    @Test
    void handlesEmptyInput() {
        assertEquals(0, BlellochScan.exclusiveScan(new long[0]).length);
    }

    @Test
    void matchesNaiveCumulativeSumForRandomData() {
        long[] input = {4, 1, 6, 2, 8, 3, 9, 5, 7, 10, 2, 6};
        long[] expected = new long[input.length];
        long running = 0;
        for (int i = 0; i < input.length; i++) {
            expected[i] = running;
            running += input[i];
        }
        assertArrayEquals(expected, BlellochScan.exclusiveScan(input));
    }
}
