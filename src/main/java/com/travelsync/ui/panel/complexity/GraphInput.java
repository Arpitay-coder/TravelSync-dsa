package com.travelsync.ui.panel.complexity;

import com.travelsync.algorithms.npcomplete.SimpleGraph;
import com.travelsync.data.AppDataStore;
import com.travelsync.model.Destination;
import com.travelsync.ui.Ui;
import com.travelsync.util.Geo;
import com.travelsync.util.ValidationException;
import com.travelsync.visualization.CellState;
import com.travelsync.visualization.GraphLayouts;
import com.travelsync.visualization.GraphVisualizer;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.IntFunction;

/**
 * Input of a "conflict graph": vertices are destinations, an edge means two destinations cannot sensibly be combined in one short
 * trip. Also draws such graphs. Shared by the independent-set / vertex-cover panels.
 */
final class GraphInput {

    static final double W = 700;
    static final double H = 380;
    private static final double TOO_FAR_KM = 12_000;

    /** How one edge is drawn. */
    record EdgeStyle(CellState state, boolean dashed, double width) { }

    private final Spinner<Integer> vertices = new Spinner<>(3, 9, 6);
    private final TextField edgesField = new TextField();
    private final Label legend = Ui.label("", "subtle-text");
    private List<String> names;

    GraphInput() {
        vertices.setEditable(true);
        vertices.setPrefWidth(80);
        edgesField.setPromptText("Edges as vertex pairs, e.g. 0-1, 1-2, 2-3");
        legend.setWrapText(true);
    }

    /** Input rows; {@code extra} rows are appended below. */
    Node grid(Node... extra) {
        Button fromData = Ui.button("Build from destinations", "btn-secondary",
                "Vertices are the first destinations; two are joined when they are more than 12,000 km apart");
        fromData.setOnAction(e -> fillFromData());
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(Ui.label("Vertices", "kv-key"), 0, 0);
        grid.add(vertices, 1, 0);
        grid.add(fromData, 2, 0);
        grid.add(Ui.label("Conflicts", "kv-key"), 0, 1);
        grid.add(edgesField, 1, 1, 2, 1);
        grid.add(legend, 0, 2, 3, 1);
        int row = 3;
        for (Node node : extra) grid.add(node, 0, row++, 3, 1);
        GridPane.setHgrow(edgesField, Priority.ALWAYS);
        return grid;
    }

    void fillFromData() {
        List<Destination> all = AppDataStore.getInstance().getDestinations();
        int n = Math.min(8, all.size());
        if (n < 3) {
            edgesField.setText("0-1, 1-2, 2-3, 3-4, 4-5, 5-0, 1-4");
            vertices.getValueFactory().setValue(6);
            names = null;
            legend.setText("");
            return;
        }
        StringBuilder edges = new StringBuilder();
        names = new ArrayList<>();
        StringBuilder key = new StringBuilder("Vertices: ");
        for (int i = 0; i < n; i++) {
            names.add(all.get(i).getName());
            key.append(i).append(" = ").append(all.get(i).getName()).append(i < n - 1 ? ", " : "");
            for (int j = i + 1; j < n; j++) {
                double km = Geo.haversineKm(all.get(i).getLatitude(), all.get(i).getLongitude(), all.get(j).getLatitude(), all.get(j).getLongitude());
                if (km > TOO_FAR_KM) edges.append(edges.length() == 0 ? "" : ", ").append(i).append("-").append(j);
            }
        }
        if (edges.length() == 0) edges.append("0-1, 1-2, 2-3");
        vertices.getValueFactory().setValue(n);
        edgesField.setText(edges.toString());
        legend.setText(key + ". An edge joins destinations more than 12,000 km apart (too far for one short trip).");
    }

    int vertexCount() throws ValidationException {
        try {
            Integer n = vertices.getValue();
            if (n == null || n < 3 || n > 9) throw new ValidationException("Please use between 3 and 9 vertices.");
            return n;
        } catch (NullPointerException ex) {
            throw new ValidationException("Please enter a whole number of vertices between 3 and 9.");
        }
    }

    SimpleGraph parse() throws ValidationException {
        int n = vertexCount();
        String raw = edgesField.getText();
        SimpleGraph g = new SimpleGraph(n);
        if (raw == null || raw.isBlank()) throw new ValidationException("Please enter at least one conflict edge such as 0-1.");
        Set<String> seen = new HashSet<>();
        for (String part : raw.split(",")) {
            String[] ends = part.trim().split("-");
            if (ends.length != 2) throw new ValidationException("\"" + part.trim() + "\" is not an edge. Write pairs like 0-1.");
            try {
                int u = Integer.parseInt(ends[0].trim());
                int v = Integer.parseInt(ends[1].trim());
                if (u < 0 || v < 0 || u >= n || v >= n) throw new ValidationException("Vertex numbers must be between 0 and " + (n - 1) + ".");
                if (u == v) throw new ValidationException("An edge needs two different vertices (" + u + "-" + v + ").");
                if (seen.add(Math.min(u, v) + "-" + Math.max(u, v))) g.addEdge(u, v);
            } catch (NumberFormatException ex) {
                throw new ValidationException("\"" + part.trim() + "\" is not an edge. Write pairs like 0-1.");
            }
        }
        return g;
    }

    /** Names of the vertices when built from destinations (else null). Copy taken at run time. */
    List<String> names(int n) {
        return names != null && names.size() == n ? List.copyOf(names) : null;
    }

    /** Draws a graph on a circle. Edge callbacks receive (u, v) with u &lt; v and may return null to hide the edge. */
    static void render(GraphVisualizer viz, int n, List<String> names, List<int[]> edges, IntFunction<CellState> nodeState,
                       IntFunction<CellState> ring, BiFunction<Integer, Integer, EdgeStyle> edgeStyle) {
        double[][] pos = GraphLayouts.circle(n, W, H);
        List<GraphVisualizer.GNode> nodes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            GraphVisualizer.GNode node = new GraphVisualizer.GNode(i, String.valueOf(i), pos[i][0], pos[i][1]);
            if (names != null) node.sub = names.get(i);
            CellState state = nodeState.apply(i);
            if (state != null) node.state = state;
            node.ring = ring == null ? null : ring.apply(i);
            nodes.add(node);
        }
        List<GraphVisualizer.GEdge> list = new ArrayList<>();
        for (int[] e : edges) {
            int u = Math.min(e[0], e[1]);
            int v = Math.max(e[0], e[1]);
            EdgeStyle style = edgeStyle.apply(u, v);
            if (style == null) continue;
            GraphVisualizer.GEdge ge = new GraphVisualizer.GEdge(u, v);
            ge.directed = false;
            ge.state = style.state();
            ge.dashed = style.dashed();
            ge.width = style.width();
            list.add(ge);
        }
        viz.render(nodes, list);
    }
}
