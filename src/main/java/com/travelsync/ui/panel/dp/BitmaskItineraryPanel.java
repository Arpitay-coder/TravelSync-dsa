package com.travelsync.ui.panel.dp;

import com.travelsync.algorithms.dp.BitmaskItineraryOptimizer;
import com.travelsync.data.AppDataStore;
import com.travelsync.model.Destination;
import com.travelsync.ui.AlgorithmPanel;
import com.travelsync.ui.Execution;
import com.travelsync.ui.Ui;
import com.travelsync.util.Geo;
import com.travelsync.util.ValidationException;
import com.travelsync.visualization.CellState;
import com.travelsync.visualization.GraphLayouts;
import com.travelsync.visualization.GraphVisualizer;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Bitmask DP panel: finds the cheapest order to visit several destinations, drawing the route leg by leg. */
public final class BitmaskItineraryPanel extends AlgorithmPanel {

    private static final double W = 700;
    private static final double H = 360;

    private final Spinner<Integer> stops = new Spinner<>(3, 8, 5);
    private final GraphVisualizer graph = new GraphVisualizer(W, H);

    public BitmaskItineraryPanel() {
        stops.setEditable(true);
        stops.setPrefWidth(90);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.add(Ui.label("Stops to visit", "kv-key"), 0, 0);
        grid.add(stops, 1, 0);
        grid.add(Ui.label("(first N destinations of the catalogue, starting at the first one)", "subtle-text"), 2, 0);
        assemble("Bitmask DP \u2013 itinerary optimisation",
                "Travel problem: choose the most efficient order of a short multi-destination trip. dp[mask][i] is the cheapest way to "
                        + "visit exactly the destinations in the bitmask and end at i (Held-Karp). Distances are real great-circle "
                        + "kilometres between destination coordinates.",
                grid, graph);
        loadDemo();
    }

    @Override
    protected void loadDemo() {
        stops.getValueFactory().setValue(5);
    }

    @Override
    protected Supplier<Execution> prepare() throws ValidationException {
        int n;
        try {
            n = stops.getValue();
        } catch (NullPointerException | NumberFormatException ex) {
            throw new ValidationException("Please enter a whole number of stops between 3 and 8.");
        }
        List<Destination> all = AppDataStore.getInstance().getDestinations();
        if (n < 3 || n > 8) throw new ValidationException("Please choose between 3 and 8 stops.");
        if (n > all.size()) throw new ValidationException("Only " + all.size() + " destinations are available.");
        List<Destination> chosen = new ArrayList<>(all.subList(0, n));
        return () -> execute(chosen);
    }

    private Execution execute(List<Destination> chosen) {
        int n = chosen.size();
        double[][] dist = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                dist[i][j] = Geo.haversineKm(chosen.get(i).getLatitude(), chosen.get(i).getLongitude(),
                        chosen.get(j).getLatitude(), chosen.get(j).getLongitude());
            }
        }
        BitmaskItineraryOptimizer.Result r = BitmaskItineraryOptimizer.solve(dist, 0);
        List<Integer> order = r.order();
        StringBuilder route = new StringBuilder();
        for (int idx : order) route.append(route.length() == 0 ? "" : " \u2192 ").append(chosen.get(idx).getName());
        long states = (1L << n) * n;
        String result = "Best route: " + route + "\nTotal distance: " + Math.round(r.totalCost()) + " km";
        String complexity = "Time O(2\u207F \u00B7 n\u00B2) = " + (states * n) + " transitions   |   Space O(2\u207F \u00B7 n) = " + states + " states";
        String info = "Trying every order by brute force would need n! = " + factorial(n - 1) + " routes from the fixed start; the DP reuses sub-results instead.";
        double[][] pos = GraphLayouts.circle(n, W, H);
        int frames = order.size();
        return new Execution(frames, f -> draw(chosen, dist, order, pos, f), f -> narrate(chosen, dist, order, f), result,
                complexity, -1, frames, info);
    }

    private static long factorial(int k) {
        long f = 1;
        for (int i = 2; i <= k; i++) f *= i;
        return f;
    }

    private void draw(List<Destination> chosen, double[][] dist, List<Integer> order, double[][] pos, int frame) {
        int n = chosen.size();
        List<GraphVisualizer.GNode> nodes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String name = chosen.get(i).getName();
            GraphVisualizer.GNode node = new GraphVisualizer.GNode(i, name.substring(0, Math.min(3, name.length())).toUpperCase(),
                    pos[i][0], pos[i][1]);
            node.sub = name;
            nodes.add(node);
        }
        List<GraphVisualizer.GEdge> edges = new ArrayList<>();
        if (n <= 5) {
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    GraphVisualizer.GEdge e = new GraphVisualizer.GEdge(i, j);
                    e.directed = false;
                    e.dashed = true;
                    e.state = CellState.MUTED;
                    edges.add(e);
                }
            }
        }
        for (int k = 0; k <= frame; k++) {
            nodes.get(order.get(k)).state = k == frame ? CellState.ACTIVE : CellState.MATCH;
            if (k > 0) {
                GraphVisualizer.GEdge e = new GraphVisualizer.GEdge(order.get(k - 1), order.get(k));
                e.state = CellState.MATCH;
                e.width = 2.6;
                e.label = Math.round(dist[order.get(k - 1)][order.get(k)]) + " km";
                edges.add(e);
            }
        }
        nodes.get(order.get(0)).ring = CellState.HIGHLIGHT;
        graph.render(nodes, edges);
    }

    private static String narrate(List<Destination> chosen, double[][] dist, List<Integer> order, int frame) {
        if (frame == 0) return "Start in " + chosen.get(order.get(0)).getName() + ". The DP has already found the cheapest order of all stops.";
        double total = 0;
        for (int k = 1; k <= frame; k++) total += dist[order.get(k - 1)][order.get(k)];
        return "Leg " + frame + ": " + chosen.get(order.get(frame - 1)).getName() + " \u2192 " + chosen.get(order.get(frame)).getName()
                + " (" + Math.round(dist[order.get(frame - 1)][order.get(frame)]) + " km). Distance so far: " + Math.round(total) + " km.";
    }
}
