package com.travelsync.ui.panel.complexity;

import com.travelsync.algorithms.npcomplete.CliqueSolver;
import com.travelsync.algorithms.npcomplete.IndependentSetAndVertexCover;
import com.travelsync.algorithms.npcomplete.SimpleGraph;
import com.travelsync.ui.AlgorithmPanel;
import com.travelsync.ui.Execution;
import com.travelsync.ui.Ui;
import com.travelsync.util.ValidationException;
import com.travelsync.visualization.CellState;
import com.travelsync.visualization.GraphVisualizer;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/** CLIQUE, INDEPENDENT SET and VERTEX COVER panel: walks a conflict graph through the reduction chain. */
public final class IndependentSetVertexCoverPanel extends AlgorithmPanel {

    private final GraphInput input = new GraphInput();
    private final Spinner<Integer> sizeSpinner = new Spinner<>(1, 9, 3);
    private final GraphVisualizer graph = new GraphVisualizer(GraphInput.W, GraphInput.H);

    public IndependentSetVertexCoverPanel() {
        sizeSpinner.setEditable(true);
        sizeSpinner.setPrefWidth(80);
        HBox sizeRow = Ui.row(10, Ui.label("Destinations to keep (independent set size k)", "kv-key"), sizeSpinner);
        assemble("CLIQUE \u2192 INDEPENDENT SET \u2192 VERTEX COVER",
                "Travel problem: some destinations conflict (too far apart for one short trip). A set of k destinations with no conflict among them "
                        + "is an independent set of the conflict graph, which is a clique of the complement graph; and the remaining destinations "
                        + "cover every conflict, forming a vertex cover of size n \u2212 k.",
                input.grid(sizeRow), graph);
        loadDemo();
    }

    @Override
    protected void loadDemo() {
        input.fillFromData();
        sizeSpinner.getValueFactory().setValue(3);
    }

    @Override
    protected Supplier<Execution> prepare() throws ValidationException {
        SimpleGraph g = input.parse();
        int n = g.size();
        int k;
        try {
            k = sizeSpinner.getValue();
        } catch (NullPointerException ex) {
            throw new ValidationException("Please enter a whole number for k.");
        }
        if (k < 1 || k > n) throw new ValidationException("k must be between 1 and the number of vertices (" + n + ").");
        List<String> names = input.names(n);
        int kk = k;
        return () -> execute(g, kk, names);
    }

    private Execution execute(SimpleGraph g, int k, List<String> names) {
        int n = g.size();
        SimpleGraph complement = g.complement();
        Optional<List<Integer>> cliqueInComplement = CliqueSolver.findClique(complement, k);
        Optional<List<Integer>> independent = IndependentSetAndVertexCover.findIndependentSet(g, k);
        List<Integer> cover = independent.map(s -> IndependentSetAndVertexCover.independentSetToVertexCover(g, s)).orElse(List.of());
        List<Integer> optimalCover = IndependentSetAndVertexCover.minimumVertexCover(g);
        boolean found = independent.isPresent();
        Set<Integer> setNodes = new HashSet<>(independent.orElse(List.of()));
        Set<Integer> coverNodes = new HashSet<>(cover);
        Set<Integer> cliqueNodes = new HashSet<>(cliqueInComplement.orElse(List.of()));
        boolean covers = found && g.edges().stream().allMatch(e -> coverNodes.contains(e[0]) || coverNodes.contains(e[1]));
        String result = found
                ? "Independent set of size " + k + ": " + independent.get() + "\nVertex cover of size " + cover.size() + " (= " + n + " \u2212 " + k + "): " + cover
                : "No independent set of size " + k + " exists, hence no vertex cover of size " + (n - k) + ".";
        String complexity = "Reductions: building the complement is O(n\u00B2) = O(" + n * n + ")   |   the searches are exponential \u2013 all three problems are NP-complete";
        String info = "Smallest possible vertex cover has " + optimalCover.size() + " vertices, so the largest independent set has " + (n - optimalCover.size())
                + ". " + (found ? "Cover check: " + (covers ? "every conflict edge has an endpoint in the cover." : "PROBLEM") : "");
        List<int[]> edges = g.edges();
        List<int[]> complementEdges = complement.edges();
        int frames = found ? 4 : 2;
        return new Execution(frames, f -> draw(g, n, names, edges, complementEdges, cliqueNodes, setNodes, coverNodes, found, f),
                f -> narrate(k, n, independent, cover, found, f), result, complexity, -1, frames, info);
    }

    private void draw(SimpleGraph g, int n, List<String> names, List<int[]> edges, List<int[]> complementEdges, Set<Integer> cliqueNodes,
                      Set<Integer> setNodes, Set<Integer> coverNodes, boolean found, int frame) {
        switch (frame) {
            case 0 -> GraphInput.render(graph, n, names, edges, i -> CellState.NORMAL, null,
                    (u, v) -> new GraphInput.EdgeStyle(CellState.NORMAL, false, 1.8));
            case 1 -> GraphInput.render(graph, n, names, complementEdges, i -> cliqueNodes.contains(i) ? CellState.HIGHLIGHT : CellState.NORMAL, null,
                    (u, v) -> cliqueNodes.contains(u) && cliqueNodes.contains(v)
                            ? new GraphInput.EdgeStyle(CellState.HIGHLIGHT, false, 3.2) : new GraphInput.EdgeStyle(CellState.MUTED, true, 1.2));
            case 2 -> GraphInput.render(graph, n, names, edges, i -> setNodes.contains(i) ? CellState.MATCH : CellState.NORMAL, null,
                    (u, v) -> new GraphInput.EdgeStyle(CellState.NORMAL, false, 1.8));
            default -> GraphInput.render(graph, n, names, edges, i -> coverNodes.contains(i) ? CellState.ACTIVE : CellState.MUTED, null,
                    (u, v) -> new GraphInput.EdgeStyle(CellState.MATCH, false, 2.8));
        }
    }

    private static String narrate(int k, int n, Optional<List<Integer>> independent, List<Integer> cover, boolean found, int frame) {
        return switch (frame) {
            case 0 -> "The conflict graph: an edge means two destinations cannot both be kept in a short trip.";
            case 1 -> found
                    ? "Complement graph (dashed = compatible pairs). A clique of size " + k + " in it (purple) is a set of " + k + " mutually compatible destinations."
                    : "In the complement graph no clique of size " + k + " exists, so no set of " + k + " mutually compatible destinations exists.";
            case 2 -> "Back in the conflict graph the same vertices (green) form an independent set: no edge joins any two of them: " + independent.orElse(List.of()) + ".";
            default -> "All other " + cover.size() + " vertices (blue) form a vertex cover: every conflict edge (green) has at least one blue end. Size n \u2212 k = " + (n - k) + ".";
        };
    }
}
