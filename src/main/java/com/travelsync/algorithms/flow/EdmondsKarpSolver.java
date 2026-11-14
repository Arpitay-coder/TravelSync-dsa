package com.travelsync.algorithms.flow;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * Edmonds-Karp maximum flow: Ford-Fulkerson specialised to always augment
 * along a shortest (fewest-edges) path, found via BFS. Used in TravelSync to
 * determine maximum flow through limited-capacity transport links with a
 * polynomial worst-case guarantee (unlike plain Ford-Fulkerson).
 *
 * Time complexity: O(V * E^2)   Space complexity: O(V + E).
 */
public final class EdmondsKarpSolver {

    private EdmondsKarpSolver() { }

    public static long solve(FlowGraph g, int source, int sink) {
        return solveTraced(g, source, sink).maxFlow();
    }

    /** Same algorithm as {@link #solve}, additionally recording every augmenting path. */
    public static FlowTrace.Result solveTraced(FlowGraph g, int source, int sink) {
        long flow = 0;
        int n = g.size();
        List<FlowTrace.Augmentation> steps = new ArrayList<>();

        while (true) {
            int[] parentNode = new int[n];
            FlowGraph.Edge[] parentEdge = new FlowGraph.Edge[n];
            Arrays.fill(parentNode, -1);
            parentNode[source] = source;

            Deque<Integer> queue = new ArrayDeque<>();
            queue.add(source);
            while (!queue.isEmpty() && parentNode[sink] == -1) {
                int u = queue.poll();
                for (FlowGraph.Edge e : g.adj(u)) {
                    if (parentNode[e.to] == -1 && e.capacity > 0) {
                        parentNode[e.to] = u;
                        parentEdge[e.to] = e;
                        queue.add(e.to);
                    }
                }
            }

            if (parentNode[sink] == -1) break; // no augmenting path left

            long bottleneck = Long.MAX_VALUE;
            List<FlowGraph.Edge> path = new ArrayList<>();
            for (int v = sink; v != source; v = parentNode[v]) {
                bottleneck = Math.min(bottleneck, parentEdge[v].capacity);
                path.add(0, parentEdge[v]);
            }
            for (FlowGraph.Edge e : path) {
                e.capacity -= bottleneck;
                g.addReverseEdgeOf(e, (int) bottleneck);
            }
            flow += bottleneck;
            steps.add(new FlowTrace.Augmentation(path, bottleneck, flow, 0));
        }
        return new FlowTrace.Result(flow, steps);
    }
}
