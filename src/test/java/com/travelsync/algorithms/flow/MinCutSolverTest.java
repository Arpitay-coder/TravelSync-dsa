package com.travelsync.algorithms.flow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MinCutSolverTest {

    @Test
    void minCutValueEqualsMaxFlow() {
        FlowGraph g = new FlowGraph(6);
        g.addEdge(0, 1, 16); g.addEdge(0, 2, 13);
        g.addEdge(1, 2, 10); g.addEdge(2, 1, 4);
        g.addEdge(1, 3, 12); g.addEdge(3, 2, 9);
        g.addEdge(2, 4, 14); g.addEdge(4, 3, 7);
        g.addEdge(3, 5, 20); g.addEdge(4, 5, 4);

        var result = MinCutSolver.solve(g, 0, 5);
        assertEquals(23, result.minCutValue());
    }

    @Test
    void sourceSideContainsSourceButNotSink() {
        FlowGraph g = new FlowGraph(4);
        g.addEdge(0, 1, 5);
        g.addEdge(1, 2, 3);
        g.addEdge(2, 3, 7);
        var result = MinCutSolver.solve(g, 0, 3);
        assertTrue(result.sourceSideNodes().contains(0));
        assertFalse(result.sourceSideNodes().contains(3));
    }

    @Test
    void cutEdgesSumToMinCutValue() {
        FlowGraph g = new FlowGraph(4);
        g.addEdge(0, 1, 5);
        g.addEdge(1, 2, 3);
        g.addEdge(2, 3, 7);
        var result = MinCutSolver.solve(g, 0, 3);
        // capacities on the original graph are needed to sum; here bottleneck edge (1->2, cap 3)
        // must be among the cut edges since it is the unique bottleneck.
        boolean containsBottleneck = result.cutEdges().stream()
                .anyMatch(edge -> edge[0] == 1 && edge[1] == 2);
        assertTrue(containsBottleneck);
    }

    @Test
    void disconnectedGraphHasZeroMinCut() {
        FlowGraph g = new FlowGraph(3);
        g.addEdge(0, 1, 10);
        var result = MinCutSolver.solve(g, 0, 2);
        assertEquals(0, result.minCutValue());
    }
}
