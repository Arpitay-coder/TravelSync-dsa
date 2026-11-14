package com.travelsync.algorithms.parallel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BrentsTheoremEvaluatorTest {

    @Test
    void upperBoundEqualsWorkOverPPlusDepth() {
        var bound = BrentsTheoremEvaluator.computeBound(1000, 10, 4);
        assertEquals(1000.0 / 4 + 10, bound.upperBound(), 1e-9);
    }

    @Test
    void lowerBoundIsMaxOfDepthAndWorkOverP() {
        var bound = BrentsTheoremEvaluator.computeBound(1000, 500, 2);
        assertEquals(500.0, bound.lowerBound(), 1e-9); // depth dominates
        var bound2 = BrentsTheoremEvaluator.computeBound(1000, 2, 2);
        assertEquals(500.0, bound2.lowerBound(), 1e-9); // work/p dominates
    }

    @Test
    void lowerBoundNeverExceedsUpperBound() {
        var bound = BrentsTheoremEvaluator.computeBound(500, 50, 8);
        assertTrue(bound.lowerBound() <= bound.upperBound());
    }

    @Test
    void idealSpeedupIsAtMostProcessorCount() {
        double speedup = BrentsTheoremEvaluator.idealSpeedup(10_000, 20, 8);
        assertTrue(speedup <= 8.0);
    }

    @Test
    void moreProcessorsNeverDecreaseIdealSpeedup() {
        double s1 = BrentsTheoremEvaluator.idealSpeedup(10_000, 20, 4);
        double s2 = BrentsTheoremEvaluator.idealSpeedup(10_000, 20, 8);
        assertTrue(s2 >= s1);
    }
}
