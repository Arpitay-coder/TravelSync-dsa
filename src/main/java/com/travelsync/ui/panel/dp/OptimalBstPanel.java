package com.travelsync.ui.panel.dp;

import com.travelsync.algorithms.dp.OptimalBSTBuilder;
import com.travelsync.data.AppDataStore;
import com.travelsync.model.Destination;
import com.travelsync.ui.AlgorithmPanel;
import com.travelsync.ui.Execution;
import com.travelsync.ui.Ui;
import com.travelsync.util.ValidationException;
import com.travelsync.visualization.CellState;
import com.travelsync.visualization.GraphVisualizer;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Optimal BST panel: lays out frequently searched destinations so the average lookup is cheapest. */
public final class OptimalBstPanel extends AlgorithmPanel {

    private static final double W = 700;
    private static final double H = 340;

    private final TextField inputField = new TextField();
    private final GraphVisualizer graph = new GraphVisualizer(W, H);

    public OptimalBstPanel() {
        inputField.setPromptText("name:frequency, name:frequency, ...");
        Button useData = Ui.button("Use popular destinations", "btn-secondary", "Use the six most popular destinations, popularity as frequency");
        useData.setOnAction(e -> inputField.setText(AppDataStore.getInstance().getDestinations().stream()
                .sorted(Comparator.comparingDouble(Destination::getPopularityScore).reversed()).limit(6)
                .map(d -> d.getName() + ":" + (int) d.getPopularityScore()).collect(Collectors.joining(", "))));
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.add(Ui.label("Search frequencies", "kv-key"), 0, 0);
        grid.add(inputField, 1, 0);
        grid.add(useData, 2, 0);
        GridPane.setHgrow(inputField, Priority.ALWAYS);
        assemble("Optimal Binary Search Tree",
                "Travel problem: some destinations and services are searched far more often than others. The optimal BST keeps keys in "
                        + "alphabetical order but chooses each subtree root so that the expected number of comparisons is minimal.",
                grid, graph);
        loadDemo();
    }

    @Override
    protected void loadDemo() {
        inputField.setText("Bali:12, Kyoto:25, Marrakech:9, Paris:18, Rome:6");
    }

    @Override
    protected Supplier<Execution> prepare() throws ValidationException {
        String raw = inputField.getText();
        if (raw == null || raw.isBlank()) throw new ValidationException("Please enter destinations with their search frequencies.");
        List<Object[]> entries = new ArrayList<>();
        for (String part : raw.split(",")) {
            String[] kv = part.trim().split(":");
            if (kv.length != 2 || kv[0].isBlank()) throw new ValidationException("Use the form name:frequency, e.g. Bali:12.");
            try {
                double f = Double.parseDouble(kv[1].trim());
                if (f <= 0) throw new ValidationException("Frequencies must be positive numbers.");
                entries.add(new Object[]{kv[0].trim(), f});
            } catch (NumberFormatException ex) {
                throw new ValidationException("\"" + kv[1].trim() + "\" is not a valid frequency.");
            }
        }
        if (entries.isEmpty() || entries.size() > 8) throw new ValidationException("Please enter between 1 and 8 destinations.");
        entries.sort(Comparator.comparing(o -> ((String) o[0]).toLowerCase()));
        String[] keys = new String[entries.size()];
        double[] freq = new double[entries.size()];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = (String) entries.get(i)[0];
            freq[i] = (Double) entries.get(i)[1];
        }
        for (int i = 1; i < keys.length; i++) {
            if (keys[i].equalsIgnoreCase(keys[i - 1])) throw new ValidationException("Each destination may appear only once.");
        }
        return () -> execute(keys, freq);
    }

    private Execution execute(String[] keys, double[] freq) {
        int n = keys.length;
        OptimalBSTBuilder.Result r = OptimalBSTBuilder.compute(keys, freq);
        List<int[]> preorder = new ArrayList<>(); // {key index, depth, parent index or -1}
        collect(r.rootTable(), 0, n - 1, 0, -1, preorder);
        int maxDepth = preorder.stream().mapToInt(p -> p[1]).max().orElse(0);
        double chain = 0;
        for (int i = 0; i < n; i++) chain += freq[i] * (i + 1);
        String tree = OptimalBSTBuilder.describeTree(keys, r.rootTable(), 0, n - 1, 0).stripTrailing();
        String result = "Minimum expected search cost: " + trim(r.minExpectedCost()) + "\nTree layout:\n" + tree;
        String complexity = "Time O(n\u00B3) = O(" + n + "\u00B3)   |   Space O(n\u00B2)";
        String info = "Inserting the keys alphabetically into a plain BST (a chain) would cost " + trim(chain) + " instead.";
        return new Execution(preorder.size(), f -> draw(keys, freq, preorder, maxDepth, f), f -> narrate(keys, freq, preorder, f),
                result, complexity, -1, preorder.size(), info);
    }

    private static void collect(int[][] root, int i, int j, int depth, int parent, List<int[]> out) {
        if (i > j) return;
        int r = root[i][j];
        out.add(new int[]{r, depth, parent});
        collect(root, i, r - 1, depth + 1, r, out);
        collect(root, r + 1, j, depth + 1, r, out);
    }

    private void draw(String[] keys, double[] freq, List<int[]> preorder, int maxDepth, int frame) {
        int n = keys.length;
        double gapY = Math.min(80, (H - 90) / Math.max(1, maxDepth));
        List<GraphVisualizer.GNode> nodes = new ArrayList<>();
        List<GraphVisualizer.GEdge> edges = new ArrayList<>();
        for (int k = 0; k <= frame; k++) {
            int[] p = preorder.get(k);
            double x = n == 1 ? W / 2 : 60 + p[0] * (W - 120) / (n - 1);
            GraphVisualizer.GNode node = new GraphVisualizer.GNode(p[0], keys[p[0]].substring(0, Math.min(3, keys[p[0]].length())).toUpperCase(),
                    x, 45 + p[1] * gapY);
            node.sub = keys[p[0]] + " (" + trim(freq[p[0]]) + ")";
            node.state = k == frame ? CellState.ACTIVE : CellState.DONE;
            nodes.add(node);
            if (p[2] >= 0) {
                GraphVisualizer.GEdge e = new GraphVisualizer.GEdge(p[2], p[0]);
                e.directed = false;
                e.state = CellState.MATCH;
                edges.add(e);
            }
        }
        graph.render(nodes, edges);
    }

    private static String narrate(String[] keys, double[] freq, List<int[]> preorder, int frame) {
        int[] p = preorder.get(frame);
        String role = p[2] < 0 ? "the root of the whole tree" : "a child of " + keys[p[2]];
        return "Place " + keys[p[0]] + " (frequency " + trim(freq[p[0]]) + ") as " + role + " at depth " + (p[1] + 1)
                + ". The DP chose it because it minimises the cost of its whole key range.";
    }

    private static String trim(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.format("%.2f", v);
    }
}
