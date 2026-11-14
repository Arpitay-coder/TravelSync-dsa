package com.travelsync.algorithms.flow;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * Dinic's maximum flow algorithm: repeatedly builds a level graph via BFS,
 * then finds a blocking flow in it via DFS with per-node edge iterators.
 * Used in TravelSync for larger transportation networks where Edmonds-Karp's
 * O(V*E^2) becomes too slow.
 *
 * Time complexity: O(V^2 * E)   Space complexity: O(V + E).
 */
public final class DinicSolver {

    private DinicSolver() { }

    public static long solve(FlowGraph g, int source, int sink) {
        return solveTraced(g, source, sink).maxFlow();
    }

    /** Same algorithm as {@link #solve}, additionally recording every augmenting path and its phase. */
    public static FlowTrace.Result solveTraced(FlowGraph g, int source, int sink) {
        long flow = 0;
        int n = g.size();
        int[] level = new int[n];
        int[] iter = new int[n];
        int phase = 0;
        List<FlowTrace.Augmentation> steps = new ArrayList<>();

        while (bfs(g, source, sink, level)) {
            phase++;
            Arrays.fill(iter, 0);
            while (true) {
                List<FlowGraph.Edge> path = new ArrayList<>();
                long pushed = dfs(g, source, sink, Long.MAX_VALUE, level, iter, path);
                if (pushed <= 0) break;
                flow += pushed;
                steps.add(new FlowTrace.Augmentation(path, pushed, flow, phase));
            }
        }
        return new FlowTrace.Result(flow, steps);
    }

    private static boolean bfs(FlowGraph g, int source, int sink, int[] level) {
        Arrays.fill(level, -1);
        level[source] = 0;
        Deque<Integer> queue = new ArrayDeque<>();
        queue.add(source);
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (FlowGraph.Edge e : g.adj(u)) {
                if (e.capacity > 0 && level[e.to] < 0) {
                    level[e.to] = level[u] + 1;
                    queue.add(e.to);
                }
            }
        }
        return level[sink] >= 0;
    }

    private static long dfs(FlowGraph g, int u, int sink, long f, int[] level, int[] iter, List<FlowGraph.Edge> path) {
        if (u == sink) return f;
        for (; iter[u] < g.adj(u).size(); iter[u]++) {
            FlowGraph.Edge e = g.adj(u).get(iter[u]);
            if (e.capacity > 0 && level[e.to] == level[u] + 1) {
                long bottleneck = dfs(g, e.to, sink, Math.min(f, e.capacity), level, iter, path);
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
