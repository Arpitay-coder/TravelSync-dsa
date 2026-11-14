package com.travelsync.algorithms.parallel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParallelReducerTest {

    @Test
    void sumsSmallArrayBelowThreshold() {
        long[] data = {1, 2, 3, 4, 5};
        assertEquals(15, ParallelReducer.reduceSum(data));
    }

    @Test
    void sumsLargeArrayAboveThreshold() {
        long[] data = new long[10_000];
        long expected = 0;
        for (int i = 0; i < data.length; i++) {
            data[i] = i;
            expected += i;
        }
        assertEquals(expected, ParallelReducer.reduceSum(data));
    }

    @Test
    void customReduceComputesMax() {
        long[] data = {3, 7, 2, 9, 4, 1, 8};
        long max = ParallelReducer.reduceCustom(data, Long.MIN_VALUE, Math::max);
        assertEquals(9, max);
    }

    @Test
    void customReduceComputesProductForSmallInput() {
        long[] data = {1, 2, 3, 4};
        long product = ParallelReducer.reduceCustom(data, 1L, (a, b) -> a * b);
        assertEquals(24, product);
    }

    @Test
    void handlesEmptyArray() {
        assertEquals(0, ParallelReducer.reduceSum(new long[0]));
    }
}
