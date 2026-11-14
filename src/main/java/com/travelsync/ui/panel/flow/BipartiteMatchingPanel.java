package com.travelsync.ui.panel.flow;

import com.travelsync.algorithms.flow.BipartiteMatchingSolver;
import com.travelsync.data.AppDataStore;
import com.travelsync.model.Destination;
import com.travelsync.model.Hotel;
import com.travelsync.model.Traveler;
import com.travelsync.ui.AlgorithmPanel;
import com.travelsync.ui.Execution;
import com.travelsync.ui.Ui;
import com.travelsync.util.ValidationException;
import com.travelsync.visualization.CellState;
import com.travelsync.visualization.GraphLayouts;
import com.travelsync.visualization.GraphVisualizer;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Bipartite matching panel: assigns each traveler to a distinct hotel that fits their preferred category. */
public final class BipartiteMatchingPanel extends AlgorithmPanel {

    private static final double W = 700;
    private static final double H = 560;

    private final GraphVisualizer graph = new GraphVisualizer(W, H);

    public BipartiteMatchingPanel() {
        Label rule = Ui.label("Eligibility: a traveler may be placed in a hotel whose destination category equals their preferred category "
                + "and that still has free rooms. Each hotel takes one traveler in this assignment round.", "subtle-text");
        rule.setWrapText(true);
        assemble("Bipartite matching \u2013 travelers to hotels",
                "Travel problem: give as many travelers as possible a suitable hotel. Kuhn's algorithm takes the travelers one by one and "
                        + "looks for an augmenting path: a free eligible hotel, or a hotel whose current guest can be moved to another eligible one.",
                rule, graph);
        loadDemo();
    }

    @Override
    protected void loadDemo() { }

    @Override
    protected Supplier<Execution> prepare() throws ValidationException {
        AppDataStore store = AppDataStore.getInstance();
        List<Traveler> travelers = List.copyOf(store.getTravelers());
        List<Hotel> hotels = List.copyOf(store.getHotels());
        if (travelers.isEmpty() || hotels.isEmpty()) throw new ValidationException("There must be at least one traveler and one hotel.");
        List<String> categories = new ArrayList<>();
        for (Hotel h : hotels) {
            Destination d = store.findDestination(h.getDestinationId());
            categories.add(d == null ? "" : d.getCategory());
        }
        return () -> execute(travelers, hotels, categories);
    }

    private Execution execute(List<Traveler> travelers, List<Hotel> hotels, List<String> categories) {
        int l = travelers.size();
        int r = hotels.size();
        List<List<Integer>> adj = new ArrayList<>();
        int edgeCount = 0;
        for (Traveler t : travelers) {
            List<Integer> eligible = new ArrayList<>();
            for (int h = 0; h < r; h++) {
                if (categories.get(h).equalsIgnoreCase(t.getPreferredCategory()) && hotels.get(h).getAvailableRooms() > 0) eligible.add(h);
            }
            edgeCount += eligible.size();
            adj.add(eligible);
        }
        BipartiteMatchingSolver.TracedResult res = BipartiteMatchingSolver.solveTraced(adj, r);
        List<BipartiteMatchingSolver.Step> steps = res.steps();
        StringBuilder unmatched = new StringBuilder();
        for (BipartiteMatchingSolver.Step s : steps) {
            if (!s.matched()) unmatched.append(unmatched.length() == 0 ? "" : ", ").append(travelers.get(s.leftVertex()).getFullName());
        }
        String result = "Matched " + res.matchCount() + " of " + l + " travelers."
                + (unmatched.length() == 0 ? "" : "\nNo hotel available for: " + unmatched);
        String complexity = "Time O(V\u00B7E) = O(" + (l + r) + "\u00B7" + edgeCount + ")   |   Space O(V + E)";
        String info = edgeCount + " eligibility edge(s) between " + l + " travelers and " + r + " hotels. "
                + "The matching is maximum: no traveler can be added without removing another.";
        double[][] pos = GraphLayouts.bipartite(l, r, W, H);
        int frames = steps.size() + 1;
        return new Execution(frames, f -> draw(travelers, hotels, adj, steps, pos, f), f -> narrate(travelers, hotels, steps, f),
                result, complexity, -1, steps.size(), info);
    }

    private void draw(List<Traveler> travelers, List<Hotel> hotels, List<List<Integer>> adj,
                      List<BipartiteMatchingSolver.Step> steps, double[][] pos, int frame) {
        int l = travelers.size();
        int r = hotels.size();
        int[] match = frame == 0 ? null : steps.get(frame - 1).matchOfRight();
        int current = frame == 0 ? -1 : steps.get(frame - 1).leftVertex();
        List<GraphVisualizer.GNode> nodes = new ArrayList<>();
        for (int i = 0; i < l; i++) {
            GraphVisualizer.GNode n = new GraphVisualizer.GNode(i, travelers.get(i).getId(), pos[i][0], pos[i][1]);
            boolean matched = false;
            if (match != null) for (int m : match) if (m == i) matched = true;
            if (i <= current) n.state = matched ? CellState.MATCH : CellState.MISMATCH;
            if (i == current) n.ring = CellState.ACTIVE;
            nodes.add(n);
        }
        for (int j = 0; j < r; j++) {
            GraphVisualizer.GNode n = new GraphVisualizer.GNode(l + j, hotels.get(j).getId(), pos[l + j][0], pos[l + j][1]);
            if (match != null && match[j] >= 0) n.state = CellState.MATCH;
            nodes.add(n);
        }
        List<GraphVisualizer.GEdge> edges = new ArrayList<>();
        for (int i = 0; i < l; i++) {
            for (int j : adj.get(i)) {
                GraphVisualizer.GEdge e = new GraphVisualizer.GEdge(i, l + j);
                e.directed = false;
                boolean chosen = match != null && match[j] == i;
                e.state = chosen ? CellState.MATCH : CellState.MUTED;
                e.dashed = !chosen;
                e.width = chosen ? 3 : 1.2;
                edges.add(e);
            }
        }
        graph.render(nodes, edges);
    }

    private static String narrate(List<Traveler> travelers, List<Hotel> hotels, List<BipartiteMatchingSolver.Step> steps, int frame) {
        if (frame == 0) return "Dashed lines show which hotels each traveler could take. Travelers are processed one at a time, top to bottom.";
        BipartiteMatchingSolver.Step s = steps.get(frame - 1);
        Traveler t = travelers.get(s.leftVertex());
        if (!s.matched()) {
            return t.getFullName() + " (" + t.getPreferredCategory() + "): no augmenting path \u2013 every eligible hotel is taken and its guest cannot move. Stays unmatched.";
        }
        int hotel = -1;
        for (int j = 0; j < s.matchOfRight().length; j++) if (s.matchOfRight()[j] == s.leftVertex()) hotel = j;
        int moved = 0;
        if (frame >= 2) {
            int[] before = steps.get(frame - 2).matchOfRight();
            for (int j = 0; j < before.length; j++) if (before[j] >= 0 && before[j] != s.matchOfRight()[j]) moved++;
        }
        return t.getFullName() + " \u2192 " + hotels.get(hotel).getName() + "."
                + (moved > 0 ? " Reached by re-assigning " + moved + " earlier traveler(s) along an augmenting path." : " A free eligible hotel was available.");
    }
}
