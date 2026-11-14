package com.travelsync.algorithms.npcomplete;

import java.util.*;

/**
 * A simple undirected graph over vertices 0..n-1, used across the M5
 * reduction chain (3-SAT -> CLIQUE -> INDEPENDENT SET -> VERTEX COVER).
 */
public class SimpleGraph {

    private final int n;
    private final Set<Long> edgeSet = new HashSet<>();
    private final List<Set<Integer>> adjacency;

    public SimpleGraph(int n) {
        this.n = n;
        adjacency = new ArrayList<>();
        for (int i = 0; i < n; i++) adjacency.add(new HashSet<>());
    }

    public int size() { return n; }

    public void addEdge(int u, int v) {
        if (u == v) return;
        edgeSet.add(key(u, v));
        adjacency.get(u).add(v);
        adjacency.get(v).add(u);
    }

    public boolean hasEdge(int u, int v) {
        return edgeSet.contains(key(u, v));
    }

    public Set<Integer> neighbors(int u) { return adjacency.get(u); }

    public int edgeCount() { return edgeSet.size(); }

    public List<int[]> edges() {
        List<int[]> result = new ArrayList<>();
        for (long k : edgeSet) {
            int u = (int) (k >> 32);
            int v = (int) (k & 0xFFFFFFFFL);
            result.add(new int[]{u, v});
        }
        return result;
    }

    /** The complement graph: an edge exists iff it does NOT exist in this graph. */
    public SimpleGraph complement() {
        SimpleGraph g = new SimpleGraph(n);
        for (int u = 0; u < n; u++) {
            for (int v = u + 1; v < n; v++) {
                if (!hasEdge(u, v)) g.addEdge(u, v);
            }
        }
        return g;
    }

    private long key(int u, int v) {
        int a = Math.min(u, v), b = Math.max(u, v);
        return ((long) a << 32) | (b & 0xFFFFFFFFL);
    }
}
