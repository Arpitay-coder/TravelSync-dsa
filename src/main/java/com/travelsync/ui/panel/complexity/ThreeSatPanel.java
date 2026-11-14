package com.travelsync.ui.panel.complexity;

import com.travelsync.algorithms.npcomplete.ThreeSatSolver;
import com.travelsync.ui.AlgorithmPanel;
import com.travelsync.ui.Execution;
import com.travelsync.ui.Ui;
import com.travelsync.util.ValidationException;
import com.travelsync.visualization.CellState;
import com.travelsync.visualization.RowTableVisualizer;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/** 3-SAT panel: travel constraints as clauses, solved by trying assignments one after another. */
public final class ThreeSatPanel extends AlgorithmPanel {

    private final TextField formulaField = new TextField();
    private final RowTableVisualizer table = new RowTableVisualizer("Clause", "Literal values", "Clause true?");

    public ThreeSatPanel() {
        formulaField.setPromptText("Clauses separated by ';', literals by spaces, e.g. 1 2 3; -1 -2 3");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(Ui.label("Clauses", "kv-key"), 0, 0);
        grid.add(formulaField, 1, 0);
        grid.add(Ui.label("Variables: x1 = wants a beach, x2 = wants mountains, x3 = wants a budget hotel, x4 = travels in summer, x5 = needs a visa. "
                + "A negative number is the opposite choice.", "subtle-text"), 0, 1, 2, 1);
        GridPane.setHgrow(formulaField, Priority.ALWAYS);
        assemble("SAT / 3-SAT \u2013 travel constraints",
                "Travel problem: a traveler's requirements are clauses such as \"beach OR mountains OR budget hotel\"; the plan is valid when one "
                        + "choice of yes/no values satisfies every clause. The solver tries the 2\u207F assignments in order and checks all clauses; "
                        + "3-SAT is NP-complete, so no polynomial method is known.",
                grid, table);
        loadDemo();
    }

    @Override
    protected void loadDemo() {
        formulaField.setText(FormulaInput.DEMO);
    }

    @Override
    protected Supplier<Execution> prepare() throws ValidationException {
        FormulaInput.Formula formula = FormulaInput.parse(formulaField.getText(), 8);
        return () -> execute(formula);
    }

    private Execution execute(FormulaInput.Formula formula) {
        int n = formula.numVars();
        List<int[]> clauses = formula.clauses();
        List<boolean[]> tried = new ArrayList<>();
        boolean found = false;
        for (int mask = 0; mask < (1 << n); mask++) {
            boolean[] a = new boolean[n + 1];
            for (int v = 1; v <= n; v++) a[v] = (mask & (1 << (v - 1))) != 0;
            tried.add(a);
            if (ThreeSatSolver.satisfies(clauses, a)) {
                found = true;
                break;
            }
        }
        Optional<boolean[]> check = ThreeSatSolver.solve(clauses, n);
        boolean[] last = tried.get(tried.size() - 1);
        String result = found ? "SATISFIABLE with " + describe(last, n) : "UNSATISFIABLE \u2013 all " + tried.size() + " assignments fail.";
        String complexity = "Time O(2\u207F \u00B7 m) = O(" + (1 << n) + " \u00B7 " + clauses.size() + ")   |   Space O(n)";
        String info = "Solver cross-check: " + (check.isPresent() == found ? "agrees" : "DISAGREES") + ". Tried " + tried.size() + " of "
                + (1 << n) + " assignments" + (found ? " before finding one." : ".");
        boolean solved = found;
        return new Execution(tried.size(), f -> draw(clauses, tried.get(f), solved && f == tried.size() - 1),
                f -> narrate(clauses, tried.get(f), n, f, solved && f == tried.size() - 1), result, complexity, tried.size(), tried.size(), info);
    }

    private void draw(List<int[]> clauses, boolean[] a, boolean solution) {
        List<String[]> rows = new ArrayList<>();
        Map<Integer, CellState> states = new HashMap<>();
        for (int i = 0; i < clauses.size(); i++) {
            int[] c = clauses.get(i);
            StringBuilder values = new StringBuilder();
            boolean clauseTrue = false;
            for (int lit : c) {
                boolean v = lit > 0 ? a[lit] : !a[-lit];
                clauseTrue |= v;
                values.append(FormulaInput.literal(lit)).append("=").append(v ? "T" : "F").append("   ");
            }
            rows.add(new String[]{FormulaInput.clause(c), values.toString().trim(), clauseTrue ? "true" : "false"});
            states.put(i, clauseTrue ? (solution ? CellState.MATCH : CellState.DONE) : CellState.MISMATCH);
        }
        table.show(rows, states);
    }

    private static String describe(boolean[] a, int n) {
        StringBuilder sb = new StringBuilder();
        for (int v = 1; v <= n; v++) sb.append(sb.length() == 0 ? "" : ", ").append("x").append(v).append("=").append(a[v] ? "true" : "false");
        return sb.toString();
    }

    private static String narrate(List<int[]> clauses, boolean[] a, int n, int frame, boolean solution) {
        String head = "Assignment " + (frame + 1) + ": " + describe(a, n) + " \u2192 ";
        if (solution) return head + "every clause is true. The formula is satisfied, so the search stops.";
        for (int i = 0; i < clauses.size(); i++) {
            if (!ThreeSatSolver.satisfies(List.of(clauses.get(i)), a)) {
                return head + "clause " + (i + 1) + " " + FormulaInput.clause(clauses.get(i)) + " is false, so this assignment fails. Try the next one.";
            }
        }
        return head + "no clause is false.";
    }
}
