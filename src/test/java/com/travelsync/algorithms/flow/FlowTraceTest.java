package com.travelsync.algorithms.flow;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlowTraceTest {

    private FlowGraph network() {
        FlowGraph g = new FlowGraph(6);
        g.addEdge(0, 1, 16); g.addEdge(0, 2, 13);
        g.addEdge(1, 2, 10); g.addEdge(2, 1, 4);
        g.addEdge(1, 3, 12); g.addEdge(3, 2, 9);
        g.addEdge(2, 4, 14); g.addEdge(4, 3, 7);
        g.addEdge(3, 5, 20); g.addEdge(4, 5, 4);
        return g;
    }

    private void assertConsistent(FlowTrace.Result r) {
        assertEquals(23, r.maxFlow());
        long sum = 0;
        long previous = 0;
        for (FlowTrace.Augmentation a : r.augmentations()) {
            assertTrue(a.bottleneck() > 0);
            sum += a.bottleneck();
            assertEquals(sum, a.totalFlow());
            assertTrue(a.totalFlow() > previous);
            previous = a.totalFlow();
            assertEquals(0, a.edges().get(0).from);
            assertEquals(5, a.edges().get(a.edges().size() - 1).to);
        }
        assertEquals(23, sum);
    }

    @Test
    void fordFulkersonTraceAddsUpToMaxFlow() {
        assertConsistent(FordFulkersonSolver.solveTraced(network(), 0, 5));
    }

    @Test
    void edmondsKarpTraceAddsUpToMaxFlow() {
        assertConsistent(EdmondsKarpSolver.solveTraced(network(), 0, 5));
    }

    @Test
    void dinicTraceAddsUpToMaxFlowAndNumbersPhases() {
        FlowTrace.Result r = DinicSolver.solveTraced(network(), 0, 5);
        assertConsistent(r);
        assertTrue(r.augmentations().stream().allMatch(a -> a.phase() >= 1));
    }

    @Test
    void edgeFlowsAfterSolvingRespectCapacityAndConservation() {
        FlowGraph g = network();
        DinicSolver.solveTraced(g, 0, 5);
        long[] balance = new long[6];
        for (int u = 0; u < 6; u++) {
            for (FlowGraph.Edge e : g.adj(u)) {
                if (!e.isForward) continue;
                int flow = FlowGraph.flowOn(e);
                assertTrue(flow >= 0 && flow <= e.originalCapacity);
                balance[e.from] -= flow;
                balance[e.to] += flow;
            }
        }
        assertEquals(-23, balance[0]);
        assertEquals(23, balance[5]);
        for (int v = 1; v < 5; v++) assertEquals(0, balance[v]);
    }

    @Test
    void bipartiteTraceMatchesUntracedResultAndHasOneStepPerVertex() {
        List<List<Integer>> adj = List.of(List.of(0, 1), List.of(0), List.of(1, 2), List.of(2));
        var traced = BipartiteMatchingSolver.solveTraced(adj, 3);
        var plain = BipartiteMatchingSolver.solve(adj, 3);
        assertEquals(plain.matchCount(), traced.matchCount());
        assertEquals(4, traced.steps().size());
        assertArrayEquals(traced.matchOfRight(), traced.steps().get(3).matchOfRight());
    }
}
