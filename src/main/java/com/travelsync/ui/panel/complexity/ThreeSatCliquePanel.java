package com.travelsync.ui.panel.complexity;

import com.travelsync.algorithms.npcomplete.CliqueSolver;
import com.travelsync.algorithms.npcomplete.SimpleGraph;
import com.travelsync.algorithms.npcomplete.ThreeSatSolver;
import com.travelsync.algorithms.npcomplete.ThreeSatToCliqueReducer;
import com.travelsync.ui.AlgorithmPanel;
import com.travelsync.ui.Execution;
import com.travelsync.ui.Ui;
import com.travelsync.util.ValidationException;
import com.travelsync.visualization.CellState;
import com.travelsync.visualization.GraphVisualizer;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/** 3-SAT to CLIQUE reduction panel: one node per literal occurrence, compatible literals of different clauses connected. */
public final class ThreeSatCliquePanel extends AlgorithmPanel {

    private static final double W = 700;
    private static final double H = 360;

    private final TextField formulaField = new TextField();
    private final GraphVisualizer graph = new GraphVisualizer(W, H);

    public ThreeSatCliquePanel() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.add(Ui.label("Clauses", "kv-key"), 0, 0);
        grid.add(formulaField, 1, 0);
        GridPane.setHgrow(formulaField, Priority.ALWAYS);
        assemble("3-SAT \u2192 CLIQUE",
                "Complexity link: every literal occurrence becomes a node, drawn in one column per clause. Two nodes are joined when they "
                        + "come from different clauses and do not contradict each other (x and \u00ACx). The formula with m clauses is satisfiable "
                        + "exactly when the graph has a clique of size m: pick one true literal per clause, all mutually compatible.",
                grid, graph);
        loadDemo();
    }

    @Override
    protected void loadDemo() {
        formulaField.setText(FormulaInput.DEMO);
    }

    @Override
    protected Supplier<Execution> prepare() throws ValidationException {
        FormulaInput.Formula formula = FormulaInput.parse(formulaField.getText(), 4);
        return () -> execute(formula);
    }

    private Execution execute(FormulaInput.Formula formula) {
        List<int[]> clauses = formula.clauses();
        ThreeSatToCliqueReducer.Reduction red = ThreeSatToCliqueReducer.reduce(clauses);
        SimpleGraph g = red.graph();
        int m = red.targetCliqueSize();
        Optional<List<Integer>> clique = CliqueSolver.findClique(g, m);
        boolean satisfiable = ThreeSatSolver.solve(clauses, formula.numVars()).isPresent();
        boolean[] derived = new boolean[formula.numVars() + 1];
        if (clique.isPresent()) {
            for (int node : clique.get()) {
                int lit = red.nodeInfo().get(node).literal();
                if (lit > 0) derived[lit] = true;
            }
        }
        boolean derivedOk = clique.isPresent() && ThreeSatSolver.satisfies(clauses, derived);
        String result = "Graph with " + g.size() + " nodes and " + g.edgeCount() + " edges; target clique size " + m + "\n"
                + (clique.isPresent() ? "Clique found: " + cliqueText(red, clique.get()) : "No clique of size " + m + " exists.");
        String complexity = "Reduction: Time O((3m)\u00B2) = O(" + (g.size() * g.size()) + ") \u2013 polynomial   |   clique search itself is exponential (NP-complete)";
        String info = "3-SAT solver says " + (satisfiable ? "satisfiable" : "unsatisfiable") + "; the clique " + (clique.isPresent() ? "exists" : "does not exist")
                + (satisfiable == clique.isPresent() ? " \u2013 consistent, as the reduction promises." : " \u2013 INCONSISTENT.")
                + (clique.isPresent() ? " Assignment read off the clique satisfies the formula: " + derivedOk + "." : "");
        int cols = clauses.size();
        double[][] pos = new double[g.size()][2];
        for (int i = 0; i < g.size(); i++) {
            int c = i / 3;
            int k = i % 3;
            pos[i][0] = cols == 1 ? W / 2 : 90 + c * (W - 180) / (cols - 1);
            pos[i][1] = 70 + k * 110;
        }
        Set<Integer> cliqueNodes = new HashSet<>(clique.orElse(List.of()));
        return new Execution(3, f -> draw(red, pos, cliqueNodes, f), f -> narrate(red, clique, derived, formula.numVars(), f),
                result, complexity, -1, 3, info);
    }

    private static String cliqueText(ThreeSatToCliqueReducer.Reduction red, List<Integer> clique) {
        StringBuilder sb = new StringBuilder();
        for (int node : clique) {
            ThreeSatToCliqueReducer.NodeInfo info = red.nodeInfo().get(node);
            sb.append(sb.length() == 0 ? "" : ", ").append(FormulaInput.literal(info.literal())).append(" (clause ").append(info.clauseIndex() + 1).append(")");
        }
        return sb.toString();
    }

    private void draw(ThreeSatToCliqueReducer.Reduction red, double[][] pos, Set<Integer> clique, int frame) {
        SimpleGraph g = red.graph();
        List<GraphVisualizer.GNode> nodes = new ArrayList<>();
        for (int i = 0; i < g.size(); i++) {
            GraphVisualizer.GNode n = new GraphVisualizer.GNode(i, FormulaInput.literal(red.nodeInfo().get(i).literal()), pos[i][0], pos[i][1]);
            n.sub = i % 3 == 0 ? "clause " + (i / 3 + 1) : "";
            if (frame == 2 && clique.contains(i)) n.state = CellState.ACTIVE;
            nodes.add(n);
        }
        List<GraphVisualizer.GEdge> edges = new ArrayList<>();
        if (frame >= 1) {
            for (int[] e : g.edges()) {
                GraphVisualizer.GEdge ge = new GraphVisualizer.GEdge(e[0], e[1]);
                ge.directed = false;
                boolean inClique = frame == 2 && clique.contains(e[0]) && clique.contains(e[1]);
                ge.state = inClique ? CellState.MATCH : CellState.MUTED;
                ge.width = inClique ? 3.2 : 1.1;
                edges.add(ge);
            }
        }
        graph.render(nodes, edges);
    }

    private static String narrate(ThreeSatToCliqueReducer.Reduction red, Optional<List<Integer>> clique, boolean[] derived, int vars, int frame) {
        return switch (frame) {
            case 0 -> "One node per literal occurrence; each column is one clause. Nodes of the same clause are never connected.";
            case 1 -> "Edges join literals from different clauses that do not contradict each other. A clique needs one node from every clause.";
            default -> clique.isPresent()
                    ? "A clique of size " + red.targetCliqueSize() + " exists: choosing those literals as true gives a satisfying assignment ("
                    + assignmentText(derived, vars) + ")."
                    : "No clique of size " + red.targetCliqueSize() + " exists, so the formula cannot be satisfied.";
        };
    }

    private static String assignmentText(boolean[] a, int n) {
        StringBuilder sb = new StringBuilder();
        for (int v = 1; v <= n; v++) sb.append(sb.length() == 0 ? "" : ", ").append("x").append(v).append("=").append(a[v] ? "T" : "F");
        return sb.toString();
    }
}
