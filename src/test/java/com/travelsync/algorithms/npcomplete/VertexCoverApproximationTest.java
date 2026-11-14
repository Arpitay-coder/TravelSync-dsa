package com.travelsync.algorithms.npcomplete;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VertexCoverApproximationTest {

    private static boolean covers(SimpleGraph g, List<Integer> cover) {
        for (int[] e : g.edges()) if (!cover.contains(e[0]) && !cover.contains(e[1])) return false;
        return true;
    }

    @Test
    void pickedEdgesAreDisjointAndFormAMaximalMatching() {
        SimpleGraph g = new SimpleGraph(6);
        g.addEdge(0, 1); g.addEdge(1, 2); g.addEdge(2, 3); g.addEdge(3, 4); g.addEdge(4, 5); g.addEdge(0, 5);
        List<int[]> picked = IndependentSetAndVertexCover.approximationEdges(g);
        Set<Integer> used = new HashSet<>();
        for (int[] e : picked) {
            assertTrue(used.add(e[0]));
            assertTrue(used.add(e[1]));
        }
        // maximal: every edge of the graph touches a picked vertex
        for (int[] e : g.edges()) assertTrue(used.contains(e[0]) || used.contains(e[1]));
    }

    @Test
    void approximationIsDeterministic() {
        SimpleGraph g = new SimpleGraph(5);
        g.addEdge(0, 1); g.addEdge(1, 2); g.addEdge(2, 3); g.addEdge(3, 4);
        assertEquals(IndependentSetAndVertexCover.twoApproximateVertexCover(g),
                IndependentSetAndVertexCover.twoApproximateVertexCover(g));
    }

    @Test
    void minimumVertexCoverOfPathIsOptimal() {
        SimpleGraph g = new SimpleGraph(5);
        g.addEdge(0, 1); g.addEdge(1, 2); g.addEdge(2, 3); g.addEdge(3, 4);
        List<Integer> min = IndependentSetAndVertexCover.minimumVertexCover(g);
        assertEquals(2, min.size());
        assertTrue(covers(g, min));
    }

    @Test
    void minimumVertexCoverOfEdgelessGraphIsEmpty() {
        assertTrue(IndependentSetAndVertexCover.minimumVertexCover(new SimpleGraph(4)).isEmpty());
    }

    @Test
    void approximationNeverExceedsTwiceTheOptimumOnRandomGraphs() {
        Random random = new Random(11);
        for (int trial = 0; trial < 25; trial++) {
            int n = 4 + random.nextInt(5);
            SimpleGraph g = new SimpleGraph(n);
            for (int u = 0; u < n; u++) for (int v = u + 1; v < n; v++) if (random.nextInt(3) == 0) g.addEdge(u, v);
            List<Integer> approx = IndependentSetAndVertexCover.twoApproximateVertexCover(g);
            List<Integer> optimal = IndependentSetAndVertexCover.minimumVertexCover(g);
            assertTrue(covers(g, approx));
            assertTrue(covers(g, optimal));
            assertTrue(approx.size() <= 2 * optimal.size(), "trial " + trial);
        }
    }
}
