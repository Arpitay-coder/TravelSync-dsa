package com.travelsync.algorithms.npcomplete;

import java.util.List;
import java.util.Optional;

/**
 * 3-SAT: given clauses of exactly 3 literals each (a positive int i means
 * variable i is true, a negative int -i means variable i is negated),
 * decides satisfiability by brute force over all 2^numVars assignments.
 * Used in TravelSync to represent and check travel constraints (e.g.
 * "traveler wants beach OR mountain OR adventure, but not urban AND budget").
 *
 * Time complexity: O(2^n * m) where n = variables, m = clauses (NP-complete
 * in general; brute force is appropriate only for small demo instances).
 */
public final class ThreeSatSolver {

    private ThreeSatSolver() { }

    /** clauses: each int[3] of nonzero literals; variables numbered 1..numVars. */
    public static Optional<boolean[]> solve(List<int[]> clauses, int numVars) {
        int totalAssignments = 1 << numVars;
        for (int mask = 0; mask < totalAssignments; mask++) {
            boolean[] assignment = new boolean[numVars + 1]; // 1-indexed
            for (int v = 1; v <= numVars; v++) {
                assignment[v] = (mask & (1 << (v - 1))) != 0;
            }
            if (satisfies(clauses, assignment)) {
                return Optional.of(assignment);
            }
        }
        return Optional.empty();
    }

    public static boolean satisfies(List<int[]> clauses, boolean[] assignment) {
        for (int[] clause : clauses) {
            boolean clauseSatisfied = false;
            for (int literal : clause) {
                int var = Math.abs(literal);
                boolean value = assignment[var];
                if ((literal > 0 && value) || (literal < 0 && !value)) {
                    clauseSatisfied = true;
                    break;
                }
            }
            if (!clauseSatisfied) return false;
        }
        return true;
    }
}
