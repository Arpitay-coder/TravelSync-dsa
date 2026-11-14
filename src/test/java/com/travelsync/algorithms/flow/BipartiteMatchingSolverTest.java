package com.travelsync.algorithms.flow;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BipartiteMatchingSolverTest {

    @Test
    void findsPerfectMatchingWhenPossible() {
        // 3 travelers, 3 hotels, each traveler eligible for exactly one distinct hotel
        List<List<Integer>> adj = List.of(
                List.of(0),
                List.of(1),
                List.of(2)
        );
        var result = BipartiteMatchingSolver.solve(adj, 3);
        assertEquals(3, result.matchCount());
    }

    @Test
    void findsMaximumMatchingWhenNotPerfect() {
        // traveler 0 and 1 both only want hotel 0 -> at most 1 of them matched
        List<List<Integer>> adj = List.of(
                List.of(0),
                List.of(0),
                List.of(1)
        );
        var result = BipartiteMatchingSolver.solve(adj, 2);
        assertEquals(2, result.matchCount());
    }

    @Test
    void handlesNoEligibleOptions() {
        List<List<Integer>> adj = List.of(List.of(), List.of());
        var result = BipartiteMatchingSolver.solve(adj, 2);
        assertEquals(0, result.matchCount());
    }

    @Test
    void matchOfRightCorrectlyReflectsAssignment() {
        List<List<Integer>> adj = List.of(
                List.of(0, 1),
                List.of(1)
        );
        var result = BipartiteMatchingSolver.solve(adj, 2);
        assertEquals(2, result.matchCount());
        assertTrue(result.matchOfRight()[0] == 0 || result.matchOfRight()[0] == 1);
    }
}
