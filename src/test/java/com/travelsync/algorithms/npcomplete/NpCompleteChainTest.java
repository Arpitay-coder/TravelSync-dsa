package com.travelsync.algorithms.npcomplete;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NpCompleteChainTest {

    @Test
    void threeSatFindsSatisfyingAssignment() {
        // (x1 OR x2 OR x3) AND (-x1 OR -x2 OR x3)
        List<int[]> clauses = List.of(new int[]{1, 2, 3}, new int[]{-1, -2, 3});
        Optional<boolean[]> result = ThreeSatSolver.solve(clauses, 3);
        assertTrue(result.isPresent());
        assertTrue(ThreeSatSolver.satisfies(clauses, result.get()));
    }

    @Test
    void threeSatDetectsUnsatisfiableFormula() {
        // x1 AND -x1 AND x1 (as three trivial clauses forcing contradiction)
        List<int[]> clauses = List.of(
                new int[]{1, 1, 1},
                new int[]{-1, -1, -1}
        );
        assertTrue(ThreeSatSolver.solve(clauses, 1).isEmpty());
    }

    @Test
    void reductionProducesCorrectNumberOfNodes() {
        List<int[]> clauses = List.of(new int[]{1, 2, 3}, new int[]{-1, -2, 3});
        var reduction = ThreeSatToCliqueReducer.reduce(clauses);
        assertEquals(6, reduction.graph().size()); // 3 literals * 2 clauses
        assertEquals(2, reduction.targetCliqueSize());
    }

    @Test
    void satisfiableFormulaYieldsCliqueOfTargetSize() {
        List<int[]> clauses = List.of(new int[]{1, 2, 3}, new int[]{-1, -2, 3});
        var reduction = ThreeSatToCliqueReducer.reduce(clauses);
        Optional<List<Integer>> clique = CliqueSolver.findClique(reduction.graph(), reduction.targetCliqueSize());
        assertTrue(clique.isPresent());
        assertEquals(reduction.targetCliqueSize(), clique.get().size());
    }

    @Test
    void independentSetInComplementMatchesCliqueInOriginal() {
        SimpleGraph g = new SimpleGraph(4);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        // Independent set {0, 2} or {0,3} or {1,3} size 2 should exist
        Optional<List<Integer>> indep = IndependentSetAndVertexCover.findIndependentSet(g, 2);
        assertTrue(indep.isPresent());
        // verify no edge exists between chosen pair
        List<Integer> set = indep.get();
        assertFalse(g.hasEdge(set.get(0), set.get(1)));
    }

    @Test
    void vertexCoverFromIndependentSetCoversAllEdges() {
        SimpleGraph g = new SimpleGraph(4);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        Optional<List<Integer>> indep = IndependentSetAndVertexCover.findIndependentSet(g, 2);
        assertTrue(indep.isPresent());
        List<Integer> cover = IndependentSetAndVertexCover.independentSetToVertexCover(g, indep.get());
        for (int[] e : g.edges()) {
            assertTrue(cover.contains(e[0]) || cover.contains(e[1]));
        }
    }

    @Test
    void twoApproximationCoversAllEdgesAndIsWithinFactorTwo() {
        SimpleGraph g = new SimpleGraph(5);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        List<Integer> cover = IndependentSetAndVertexCover.twoApproximateVertexCover(g);
        for (int[] e : g.edges()) {
            assertTrue(cover.contains(e[0]) || cover.contains(e[1]));
        }
        // Optimal vertex cover of this path graph (5 nodes, 4 edges) has size 2 ({1,3});
        // the 2-approximation must be no more than 2x optimal = 4.
        assertTrue(cover.size() <= 4);
    }
}
