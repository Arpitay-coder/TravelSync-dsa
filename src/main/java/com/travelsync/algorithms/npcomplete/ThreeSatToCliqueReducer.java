package com.travelsync.algorithms.npcomplete;

import java.util.ArrayList;
import java.util.List;

/**
 * Classic polynomial-time reduction: 3-SAT with m clauses is satisfiable iff
 * the constructed graph (one node per literal occurrence, 3m nodes total)
 * has a clique of size m. Two nodes are connected iff they come from
 * different clauses AND are not literal-negations of each other.
 *
 * Time complexity of the reduction itself: O((3m)^2).
 */
public final class ThreeSatToCliqueReducer {

    public record NodeInfo(int clauseIndex, int literal) { }

    public record Reduction(SimpleGraph graph, int targetCliqueSize, List<NodeInfo> nodeInfo) { }

    private ThreeSatToCliqueReducer() { }

    public static Reduction reduce(List<int[]> clauses) {
        int m = clauses.size();
        List<NodeInfo> nodeInfo = new ArrayList<>();
        for (int c = 0; c < m; c++) {
            for (int literal : clauses.get(c)) {
                nodeInfo.add(new NodeInfo(c, literal));
            }
        }

        int n = nodeInfo.size();
        SimpleGraph g = new SimpleGraph(n);
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                NodeInfo a = nodeInfo.get(i);
                NodeInfo b = nodeInfo.get(j);
                boolean differentClause = a.clauseIndex() != b.clauseIndex();
                boolean negations = a.literal() == -b.literal();
                if (differentClause && !negations) {
                    g.addEdge(i, j);
                }
            }
        }
        return new Reduction(g, m, nodeInfo);
    }
}
