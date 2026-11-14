package com.travelsync.algorithms.parallel;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ParallelTraceTest {

    @Test
    void quickSortTraceEndsSortedDescendingAndCountsComparisons() {
        int[] input = {70, 95, 60, 88, 75, 92, 81};
        List<RandomizedQuickSort.Frame> frames = RandomizedQuickSort.trace(input, true, new Random(5));
        RandomizedQuickSort.Frame last = frames.get(frames.size() - 1);
        int[] expected = input.clone();
        Arrays.sort(expected);
        for (int i = 0; i < expected.length; i++) assertEquals(expected[expected.length - 1 - i], last.data()[i]);
        assertTrue(last.comparisons() >= input.length - 1);
        for (boolean fixed : last.fixed()) assertTrue(fixed);
    }

    @Test
    void quickSortTraceKeepsTheSameMultisetInEveryFrame() {
        int[] input = {5, 3, 8, 3, 9, 1, 5};
        int[] sortedInput = input.clone();
        Arrays.sort(sortedInput);
        for (RandomizedQuickSort.Frame f : RandomizedQuickSort.trace(input, false, new Random(9))) {
            int[] copy = f.data().clone();
            Arrays.sort(copy);
            assertArrayEquals(sortedInput, copy);
        }
    }

    @Test
    void quickSortTraceHandlesSingleElementAndAlreadySortedInput() {
        assertEquals(1, RandomizedQuickSort.trace(new int[]{4}, false, new Random(1)).get(0).data().length);
        List<RandomizedQuickSort.Frame> frames = RandomizedQuickSort.trace(new int[]{1, 2, 3, 4, 5}, false, new Random(2));
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, frames.get(frames.size() - 1).data());
    }

    @Test
    void millerRabinTraceRecognisesPrimesAndCompositesWithWitnesses() {
        assertTrue(MillerRabinTester.trace(BigInteger.valueOf(97), 8, new Random(1)).probablyPrime());
        MillerRabinTester.Trace composite = MillerRabinTester.trace(BigInteger.valueOf(561), 20, new Random(1));
        assertFalse(composite.probablyPrime());
        assertTrue(composite.rounds().get(composite.rounds().size() - 1).provesComposite());
    }

    @Test
    void millerRabinTraceDecomposesNMinusOne() {
        MillerRabinTester.Trace t = MillerRabinTester.trace(BigInteger.valueOf(97), 3, new Random(1));
        // 96 = 2^5 * 3
        assertEquals(5, t.r());
        assertEquals(BigInteger.valueOf(3), t.d());
        assertEquals(BigInteger.valueOf(96), t.d().shiftLeft(t.r()));
    }

    @Test
    void millerRabinTraceHandlesTrivialInputsWithoutWitnesses() {
        assertTrue(MillerRabinTester.trace(BigInteger.valueOf(2), 5, new Random(1)).probablyPrime());
        assertFalse(MillerRabinTester.trace(BigInteger.valueOf(100), 5, new Random(1)).probablyPrime());
        assertFalse(MillerRabinTester.trace(BigInteger.ONE, 5, new Random(1)).probablyPrime());
        assertTrue(MillerRabinTester.trace(BigInteger.valueOf(100), 5, new Random(1)).rounds().isEmpty());
    }

    @Test
    void blellochTraceMatchesExclusiveScanAndHasExpectedPhases() {
        long[] input = {5, 3, 7, 2, 9, 4, 1, 6};
        BlellochScan.Trace t = BlellochScan.trace(input);
        assertArrayEquals(BlellochScan.exclusiveScan(input), t.result());
        assertEquals(8, t.paddedSize());
        // input + 3 up-sweep levels + root + 3 down-sweep levels
        assertEquals(8, t.steps().size());
        assertEquals("root", t.steps().get(4).phase());
        assertEquals(0, t.steps().get(4).snapshot()[7]);
    }

    @Test
    void blellochTraceOfNonPowerOfTwoIsPadded() {
        BlellochScan.Trace t = BlellochScan.trace(new long[]{1, 2, 3, 4, 5});
        assertEquals(8, t.paddedSize());
        assertArrayEquals(new long[]{0, 1, 3, 6, 10}, t.result());
    }

    @Test
    void reductionLevelsHalveUntilOneValueEqualToTheSum() {
        long[] data = {3, 1, 4, 1, 5, 9, 2, 6};
        List<long[]> levels = ParallelReducer.reductionLevels(data);
        assertEquals(4, levels.size());
        assertEquals(1, levels.get(3).length);
        assertEquals(31, levels.get(3)[0]);
        assertEquals(4, levels.get(1).length);
    }

    @Test
    void reductionLevelsCarryAnOddElement() {
        List<long[]> levels = ParallelReducer.reductionLevels(new long[]{1, 2, 3, 4, 5});
        assertEquals(15, levels.get(levels.size() - 1)[0]);
    }

    @Test
    void reduceWithExplicitParallelismEqualsSequentialSum() {
        long[] data = new long[50_000];
        long expected = 0;
        for (int i = 0; i < data.length; i++) { data[i] = i % 97; expected += data[i]; }
        assertEquals(expected, ParallelReducer.reduceSum(data, 1));
        assertEquals(expected, ParallelReducer.reduceSum(data, 4));
    }
}
