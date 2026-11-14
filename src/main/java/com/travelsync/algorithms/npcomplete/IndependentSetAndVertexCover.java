package com.travelsync.algorithms.npcomplete;

import java.util.*;

/**
 * CLIQUE -> INDEPENDENT SET -> VERTEX COVER, completing the M5 reduction
 * chain, plus the standard 2-approximation algorithm for Vertex Cover.
 *
 * Fact used throughout: for a graph G with vertex set V,
 *   S is an independent set of G  <=>  S is a clique of complement(G)
 *   S is an independent set of G  <=>  V \ S is a vertex cover of G
 */
public final class IndependentSetAndVertexCover {

    private IndependentSetAndVertexCover() { }

    /** Independent Set in g of the given size, found via CLIQUE on the complement graph. */
    public static Optional<List<Integer>> findIndependentSet(SimpleGraph g, int targetSize) {
        SimpleGraph complement = g.complement();
        return CliqueSolver.findClique(complement, targetSize);
    }

    /** Converts an independent set into its corresponding vertex cover (V minus the independent set). */
    public static List<Integer> independentSetToVertexCover(SimpleGraph g, List<Integer> independentSet) {
        Set<Integer> inSet = new HashSet<>(independentSet);
        List<Integer> cover = new ArrayList<>();
        for (int v = 0; v < g.size(); v++) {
            if (!inSet.contains(v)) cover.add(v);
        }
        return cover;
    }

    /**
     * An exact minimum vertex cover, found through the chain above: the largest independent set's complement.
     * Exponential in general (the problem is NP-hard); intended for the small graphs of the demo.
     */
    public static List<Integer> minimumVertexCover(SimpleGraph g) {
        for (int size = g.size(); size >= 0; size--) {
            Optional<List<Integer>> independent = findIndependentSet(g, size);
            if (independent.isPresent()) return independentSetToVertexCover(g, independent.get());
        }
        List<Integer> all = new ArrayList<>();
        for (int v = 0; v < g.size(); v++) all.add(v);
        return all;
    }

    /**
     * The edges picked by the 2-approximation, in order: repeatedly take the smallest remaining edge (u, v) and
     * discard every edge touching u or v. The picked edges form a maximal matching.
     */
    public static List<int[]> approximationEdges(SimpleGraph g) {
        TreeSet<Long> remaining = new TreeSet<>();
        for (int[] e : g.edges()) {
            remaining.add(((long) Math.min(e[0], e[1]) << 32) | (Math.max(e[0], e[1]) & 0xFFFFFFFFL));
        }
        List<int[]> picked = new ArrayList<>();
        while (!remaining.isEmpty()) {
            long edgeKey = remaining.first();
            int u = (int) (edgeKey >> 32);
            int v = (int) (edgeKey & 0xFFFFFFFFL);
            picked.add(new int[]{u, v});
            remaining.removeIf(k -> {
                int a = (int) (k >> 32);
                int b = (int) (k & 0xFFFFFFFFL);
                return a == u || b == u || a == v || b == v;
            });
        }
        return picked;
    }

    /**
     * Standard 2-approximation for Vertex Cover: add BOTH endpoints of every edge picked by
     * {@link #approximationEdges}. Guaranteed within 2x the optimal size, because any cover must contain at
     * least one endpoint of each picked (pairwise disjoint) edge.
     *
     * Time complexity: O(V + E) up to the ordered-set overhead.
     */
    public static List<Integer> twoApproximateVertexCover(SimpleGraph g) {
        TreeSet<Integer> cover = new TreeSet<>();
        for (int[] e : approximationEdges(g)) {
            cover.add(e[0]);
            cover.add(e[1]);
        }
        return new ArrayList<>(cover);
    }
}
