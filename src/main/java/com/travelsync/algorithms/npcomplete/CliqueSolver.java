package com.travelsync.algorithms.npcomplete;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Finds a clique of a given target size via backtracking. CLIQUE is
 * NP-complete; this exhaustive search is appropriate only for the small
 * graphs produced by the 3-SAT reduction in a teaching/demo context.
 *
 * Time complexity: O(n^k) worst case (bounded by exponential search with
 * pruning in practice).
 */
public final class CliqueSolver {

    private CliqueSolver() { }

    public static Optional<List<Integer>> findClique(SimpleGraph g, int targetSize) {
        List<Integer> current = new ArrayList<>();
        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < g.size(); i++) candidates.add(i);
        List<Integer> result = backtrack(g, current, candidates, targetSize);
        return Optional.ofNullable(result);
    }

    private static List<Integer> backtrack(SimpleGraph g, List<Integer> current, List<Integer> candidates, int targetSize) {
        if (current.size() == targetSize) {
            return new ArrayList<>(current);
        }
        for (int i = 0; i < candidates.size(); i++) {
            int v = candidates.get(i);
            boolean connectedToAll = true;
            for (int u : current) {
                if (!g.hasEdge(u, v)) { connectedToAll = false; break; }
            }
            if (!connectedToAll) continue;

            current.add(v);
            List<Integer> remaining = new ArrayList<>();
            for (int j = i + 1; j < candidates.size(); j++) {
                if (g.hasEdge(v, candidates.get(j))) remaining.add(candidates.get(j));
            }
            List<Integer> found = backtrack(g, current, remaining, targetSize);
            if (found != null) return found;
            current.remove(current.size() - 1);
        }
        return null;
    }
}
