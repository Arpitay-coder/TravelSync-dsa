package com.travelsync.ui.panel.complexity;

import com.travelsync.util.ValidationException;

import java.util.ArrayList;
import java.util.List;

/** Parsing and formatting of 3-SAT formulas typed as "1 2 3; -1 -2 3; ...". */
final class FormulaInput {

    record Formula(List<int[]> clauses, int numVars) { }

    static final String DEMO = "1 2 3; -1 -2 3; 1 -2 -3";

    private FormulaInput() { }

    static Formula parse(String text, int maxClauses) throws ValidationException {
        if (text == null || text.isBlank()) throw new ValidationException("Please enter the clauses first.");
        List<int[]> clauses = new ArrayList<>();
        int maxVar = 0;
        for (String part : text.split(";")) {
            if (part.isBlank()) continue;
            String[] tokens = part.trim().split("[\\s,]+");
            if (tokens.length != 3) {
                throw new ValidationException("Every clause needs exactly three literals (3-SAT), but \"" + part.trim() + "\" has " + tokens.length + ".");
            }
            int[] clause = new int[3];
            for (int i = 0; i < 3; i++) {
                try {
                    clause[i] = Integer.parseInt(tokens[i]);
                } catch (NumberFormatException ex) {
                    throw new ValidationException("\"" + tokens[i] + "\" is not a whole number. Write literals like 2 or -2.");
                }
                if (clause[i] == 0) throw new ValidationException("Variables are numbered from 1; use -1 for the negation of x1.");
                maxVar = Math.max(maxVar, Math.abs(clause[i]));
            }
            clauses.add(clause);
        }
        if (clauses.isEmpty()) throw new ValidationException("Please enter at least one clause.");
        if (clauses.size() > maxClauses) throw new ValidationException("Please use at most " + maxClauses + " clauses.");
        if (maxVar > 5) throw new ValidationException("Please use at most 5 variables (x1 to x5).");
        return new Formula(clauses, maxVar);
    }

    static String literal(int lit) {
        return lit > 0 ? "x" + lit : "\u00ACx" + (-lit);
    }

    static String clause(int[] c) {
        return "(" + literal(c[0]) + " \u2228 " + literal(c[1]) + " \u2228 " + literal(c[2]) + ")";
    }
}
