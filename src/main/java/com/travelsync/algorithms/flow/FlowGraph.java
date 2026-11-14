package com.travelsync.algorithms.flow;

import java.util.ArrayList;
import java.util.List;

/**
 * A directed flow network represented with residual edges (the standard
 * adjacency-list + reverse-edge-index representation used by Ford-Fulkerson,
 * Edmonds-Karp and Dinic). Used in TravelSync to model passenger/transport
 * capacity between destinations (M4).
 */
public class FlowGraph {

    public static class Edge {
        public final int to;
        public int capacity;
        public final int reverseIndex; // index of the reverse edge in graph[to]
        public final int from;
        public final boolean isForward; // true = a real network edge, false = its residual counterpart
        public final int originalCapacity; // capacity the edge was created with (0 for residual edges)

        Edge(int from, int to, int capacity, int reverseIndex, boolean isForward, int originalCapacity) {
            this.from = from;
            this.to = to;
            this.capacity = capacity;
            this.reverseIndex = reverseIndex;
            this.isForward = isForward;
            this.originalCapacity = originalCapacity;
        }
    }

    private final int n;
    private final List<List<Edge>> graph;

    public FlowGraph(int n) {
        this.n = n;
        graph = new ArrayList<>();
        for (int i = 0; i < n; i++) graph.add(new ArrayList<>());
    }

    public int size() { return n; }

    public List<Edge> adj(int u) { return graph.get(u); }

    public void addEdge(int from, int to, int capacity) {
        Edge forward = new Edge(from, to, capacity, graph.get(to).size(), true, capacity);
        Edge backward = new Edge(to, from, 0, graph.get(from).size(), false, 0);
        graph.get(from).add(forward);
        graph.get(to).add(backward);
    }

    /** Flow currently carried by a forward edge. */
    public static int flowOn(Edge forward) {
        return forward.originalCapacity - forward.capacity;
    }

    public void addReverseEdgeOf(Edge e, int delta) {
        graph.get(e.to).get(e.reverseIndex).capacity += delta;
    }
}
