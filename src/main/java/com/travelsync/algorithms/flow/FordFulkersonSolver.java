package com.travelsync.algorithms.flow;

import java.util.ArrayList;
import java.util.List;

/**
 * Ford-Fulkerson maximum flow using DFS to find augmenting paths.
 * Used in TravelSync to model maximum passenger throughput through a chain
 * of transportation links between destinations.
 *
 * Time complexity: O(E * maxFlow) in general (pseudo-polynomial for integer
 * capacities, since each DFS augmentation increases flow by at least 1).
 * Space complexity: O(V + E).
 */
public final class FordFulkersonSolver {

    private FordFulkersonSolver() { }

    public static long solve(FlowGraph g, int source, int sink) {
        return solveTraced(g, source, sink).maxFlow();
    }

    /** Same algorithm as {@link #solve}, additionally recording every augmenting path. */
    public static FlowTrace.Result solveTraced(FlowGraph g, int source, int sink) {
        long flow = 0;
        List<FlowTrace.Augmentation> steps = new ArrayList<>();
        while (true) {
            boolean[] visited = new boolean[g.size()];
            List<FlowGraph.Edge> path = new ArrayList<>();
            long pushed = dfs(g, source, sink, Long.MAX_VALUE, visited, path);
            if (pushed == 0) break;
            flow += pushed;
            steps.add(new FlowTrace.Augmentation(path, pushed, flow, 0));
        }
        return new FlowTrace.Result(flow, steps);
    }

    private static long dfs(FlowGraph g, int u, int sink, long f, boolean[] visited, List<FlowGraph.Edge> path) {
        if (u == sink) return f;
        visited[u] = true;
        for (FlowGraph.Edge e : g.adj(u)) {
            if (!visited[e.to] && e.capacity > 0) {
                long bottleneck = dfs(g, e.to, sink, Math.min(f, e.capacity), visited, path);
                if (bottleneck > 0) {
                    e.capacity -= bottleneck;
                    g.addReverseEdgeOf(e, (int) bottleneck);
                    path.add(0, e);
                    return bottleneck;
                }
            }
        }
        return 0;
    }
}
