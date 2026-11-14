package com.travelsync.algorithms.flow;

import java.util.*;

/**
 * Minimum s-t cut, derived from a max-flow run via the max-flow min-cut
 * theorem: after computing max flow, the set of nodes reachable from the
 * source in the residual graph defines one side of the cut, and the
 * original edges crossing from that side to the rest are the min cut.
 * Used in TravelSync to identify transportation bottlenecks.
 *
 * Time complexity: O(max-flow algorithm) + O(V + E) for the final scan.
 */
public final class MinCutSolver {

    public record Result(long minCutValue, List<int[]> cutEdges, Set<Integer> sourceSideNodes) { }

    private MinCutSolver() { }

    /** Runs Dinic's algorithm internally, then extracts the min cut from the residual graph. */
    public static Result solve(FlowGraph g, int source, int sink) {
        long maxFlow = DinicSolver.solve(g, source, sink);

        boolean[] visited = new boolean[g.size()];
        Deque<Integer> queue = new ArrayDeque<>();
        queue.add(source);
        visited[source] = true;
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (FlowGraph.Edge e : g.adj(u)) {
                if (e.capacity > 0 && !visited[e.to]) {
                    visited[e.to] = true;
                    queue.add(e.to);
                }
            }
        }

        List<int[]> cutEdges = new ArrayList<>();
        Set<Integer> sourceSide = new HashSet<>();
        for (int u = 0; u < g.size(); u++) {
            if (visited[u]) sourceSide.add(u);
        }
        for (int u : sourceSide) {
            for (FlowGraph.Edge e : g.adj(u)) {
                if (e.isForward && !visited[e.to]) {
                    cutEdges.add(new int[]{u, e.to});
                }
            }
        }

        return new Result(maxFlow, cutEdges, sourceSide);
    }
}
