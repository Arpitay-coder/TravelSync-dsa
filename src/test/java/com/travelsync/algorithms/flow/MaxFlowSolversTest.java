package com.travelsync.algorithms.flow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaxFlowSolversTest {

    /** Classic textbook flow network: source=0, sink=5, known max flow = 23. */
    private FlowGraph classicNetwork() {
        FlowGraph g = new FlowGraph(6);
        g.addEdge(0, 1, 16);
        g.addEdge(0, 2, 13);
        g.addEdge(1, 2, 10);
        g.addEdge(2, 1, 4);
        g.addEdge(1, 3, 12);
        g.addEdge(3, 2, 9);
        g.addEdge(2, 4, 14);
        g.addEdge(4, 3, 7);
        g.addEdge(3, 5, 20);
        g.addEdge(4, 5, 4);
        return g;
    }

    @Test
    void fordFulkersonFindsKnownMaxFlow() {
        assertEquals(23, FordFulkersonSolver.solve(classicNetwork(), 0, 5));
    }

    @Test
    void edmondsKarpFindsKnownMaxFlow() {
        assertEquals(23, EdmondsKarpSolver.solve(classicNetwork(), 0, 5));
    }

    @Test
    void dinicFindsKnownMaxFlow() {
        assertEquals(23, DinicSolver.solve(classicNetwork(), 0, 5));
    }

    @Test
    void allThreeAlgorithmsAgreeOnASimpleChain() {
        FlowGraph g1 = new FlowGraph(4);
        g1.addEdge(0, 1, 5); g1.addEdge(1, 2, 3); g1.addEdge(2, 3, 7);
        FlowGraph g2 = new FlowGraph(4);
        g2.addEdge(0, 1, 5); g2.addEdge(1, 2, 3); g2.addEdge(2, 3, 7);
        FlowGraph g3 = new FlowGraph(4);
        g3.addEdge(0, 1, 5); g3.addEdge(1, 2, 3); g3.addEdge(2, 3, 7);

        long ff = FordFulkersonSolver.solve(g1, 0, 3);
        long ek = EdmondsKarpSolver.solve(g2, 0, 3);
        long dn = DinicSolver.solve(g3, 0, 3);

        assertEquals(3, ff); // bottleneck edge capacity
        assertEquals(ff, ek);
        assertEquals(ff, dn);
    }

    @Test
    void zeroFlowWhenSourceDisconnectedFromSink() {
        FlowGraph g = new FlowGraph(3);
        g.addEdge(0, 1, 10);
        // no edge to node 2 (sink)
        assertEquals(0, EdmondsKarpSolver.solve(g, 0, 2));
    }
}
