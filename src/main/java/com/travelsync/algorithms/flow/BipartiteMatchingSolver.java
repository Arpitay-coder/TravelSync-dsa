package com.travelsync.algorithms.flow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Maximum bipartite matching via Kuhn's algorithm (augmenting paths).
 * Used in TravelSync to assign travelers to eligible hotels or services
 * (e.g. matching by preferred category and availability).
 *
 * Time complexity: O(V * E)   Space complexity: O(V + E).
 */
public final class BipartiteMatchingSolver {

    public record Result(int matchCount, int[] matchOfRight) { }

    /** State after processing one left vertex: whether it found a partner and the whole matching at that moment. */
    public record Step(int leftVertex, boolean matched, int[] matchOfRight) { }

    public record TracedResult(int matchCount, int[] matchOfRight, List<Step> steps) { }

    private BipartiteMatchingSolver() { }

    /** adjLeftToRight.get(u) = list of right-side indices that left node u is eligible for. */
    public static Result solve(List<List<Integer>> adjLeftToRight, int rightCount) {
        TracedResult r = solveTraced(adjLeftToRight, rightCount);
        return new Result(r.matchCount(), r.matchOfRight());
    }

    /** Same algorithm as {@link #solve}, recording the matching after every left vertex is processed. */
    public static TracedResult solveTraced(List<List<Integer>> adjLeftToRight, int rightCount) {
        int leftCount = adjLeftToRight.size();
        int[] matchOfRight = new int[rightCount];
        Arrays.fill(matchOfRight, -1);
        int matches = 0;
        List<Step> steps = new ArrayList<>();

        for (int u = 0; u < leftCount; u++) {
            boolean[] visited = new boolean[rightCount];
            boolean matched = tryAugment(u, adjLeftToRight, visited, matchOfRight);
            if (matched) matches++;
            steps.add(new Step(u, matched, matchOfRight.clone()));
        }
        return new TracedResult(matches, matchOfRight, steps);
    }

    private static boolean tryAugment(int u, List<List<Integer>> adj, boolean[] visited, int[] matchOfRight) {
        for (int v : adj.get(u)) {
            if (!visited[v]) {
                visited[v] = true;
                if (matchOfRight[v] == -1 || tryAugment(matchOfRight[v], adj, visited, matchOfRight)) {
                    matchOfRight[v] = u;
                    return true;
                }
            }
        }
        return false;
    }
}
