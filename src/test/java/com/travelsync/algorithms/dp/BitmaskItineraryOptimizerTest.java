package com.travelsync.algorithms.dp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BitmaskItineraryOptimizerTest {

    @Test
    void findsOptimalOrderForSimpleTriangle() {
        // 0 -> 1 costs 1, 0 -> 2 costs 10, 1 -> 2 costs 1, 2 -> 1 costs 1
        double[][] dist = {
                {0, 1, 10},
                {1, 0, 1},
                {10, 1, 0}
        };
        var result = BitmaskItineraryOptimizer.solve(dist, 0);
        assertEquals(2.0, result.totalCost(), 1e-9);
        assertEquals(java.util.List.of(0, 1, 2), result.order());
    }

    @Test
    void handlesSingleDestination() {
        double[][] dist = {{0}};
        var result = BitmaskItineraryOptimizer.solve(dist, 0);
        assertEquals(0, result.totalCost());
        assertEquals(java.util.List.of(0), result.order());
    }

    @Test
    void orderAlwaysStartsAtGivenOrigin() {
        double[][] dist = {
                {0, 4, 8, 3},
                {4, 0, 6, 5},
                {8, 6, 0, 2},
                {3, 5, 2, 0}
        };
        var result = BitmaskItineraryOptimizer.solve(dist, 2);
        assertEquals(2, result.order().get(0));
        assertEquals(4, result.order().size());
    }

    @Test
    void visitsEveryDestinationExactlyOnce() {
        double[][] dist = {
                {0, 2, 9, 10},
                {1, 0, 6, 4},
                {15, 7, 0, 8},
                {6, 3, 12, 0}
        };
        var result = BitmaskItineraryOptimizer.solve(dist, 0);
        assertEquals(java.util.Set.of(0, 1, 2, 3), new java.util.HashSet<>(result.order()));
        assertEquals(4, result.order().size());
    }
}
