package com.travelsync.ui.panel.complexity;

import com.travelsync.algorithms.npcomplete.IndependentSetAndVertexCover;
import com.travelsync.algorithms.npcomplete.SimpleGraph;
import com.travelsync.ui.AlgorithmPanel;
import com.travelsync.ui.Execution;
import com.travelsync.util.ValidationException;
import com.travelsync.visualization.CellState;
import com.travelsync.visualization.GraphVisualizer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/** Vertex Cover 2-approximation panel: takes both ends of any uncovered edge until every edge is covered. */
public final class VertexCoverApproxPanel extends AlgorithmPanel {

    private final GraphInput input = new GraphInput();
    private final GraphVisualizer graph = new GraphVisualizer(GraphInput.W, GraphInput.H);

    public VertexCoverApproxPanel() {
        assemble("Vertex Cover \u2013 2-approximation",
                "Travel problem: choose a small set of hub destinations such that every important connection touches at least one hub. Finding the "
                        + "smallest such set is NP-hard, but repeatedly taking BOTH ends of an uncovered connection is fast and never uses more than "
                        + "twice the optimal number of hubs.",
                input.grid(), graph);
        loadDemo();
    }

    @Override
    protected void loadDemo() {
        input.fillFromData();
    }

    @Override
    protected Supplier<Execution> prepare() throws ValidationException {
        SimpleGraph g = input.parse();
        List<String> names = input.names(g.size());
        return () -> execute(g, names);
    }

    private Execution execute(SimpleGraph g, List<String> names) {
        int n = g.size();
        List<int[]> picked = IndependentSetAndVertexCover.approximationEdges(g);
        List<Integer> approx = IndependentSetAndVertexCover.twoApproximateVertexCover(g);
        List<Integer> optimal = IndependentSetAndVertexCover.minimumVertexCover(g);
        List<int[]> edges = g.edges();
        Set<Integer> optimalSet = new HashSet<>(optimal);
        int frames = picked.size() + 2;
        double ratio = optimal.isEmpty() ? 1.0 : (double) approx.size() / optimal.size();
        String result = "2-approximate cover: " + approx + " (size " + approx.size() + ")\nOptimal cover: " + optimal + " (size " + optimal.size()
                + ")\nRatio " + String.format("%.2f", ratio) + " \u2264 2";
        String complexity = "Approximation: Time O(V + E) = O(" + (n + edges.size()) + ")   |   exact minimum cover is NP-hard (exponential search here)";
        String info = picked.size() + " edge(s) were picked; they share no endpoint, so any cover needs at least " + picked.size()
                + " vertices, which proves the factor 2.";
        return new Execution(frames, f -> draw(n, names, edges, picked, optimalSet, f), f -> narrate(g, picked, approx, optimal, f),
                result, complexity, -1, picked.size(), info);
    }

    private void draw(int n, List<String> names, List<int[]> edges, List<int[]> picked, Set<Integer> optimal, int frame) {
        int used = Math.min(frame, picked.size());
        Set<Integer> cover = new HashSet<>();
        for (int i = 0; i < used; i++) {
            cover.add(picked.get(i)[0]);
            cover.add(picked.get(i)[1]);
        }
        int[] current = frame >= 1 && frame <= picked.size() ? picked.get(frame - 1) : null;
        boolean last = frame == picked.size() + 1;
        GraphInput.render(graph, n, names, edges, i -> cover.contains(i) ? CellState.ACTIVE : CellState.NORMAL,
                i -> last && optimal.contains(i) ? CellState.HIGHLIGHT : null,
                (u, v) -> {
                    if (current != null && u == Math.min(current[0], current[1]) && v == Math.max(current[0], current[1])) {
                        return new GraphInput.EdgeStyle(CellState.MATCH, false, 4);
                    }
                    boolean covered = cover.contains(u) || cover.contains(v);
                    return new GraphInput.EdgeStyle(covered ? CellState.DONE : CellState.NORMAL, false, covered ? 2.2 : 1.8);
                });
    }

    private static String narrate(SimpleGraph g, List<int[]> picked, List<Integer> approx, List<Integer> optimal, int frame) {
        if (frame == 0) return "Every edge is still uncovered. Repeat: take an uncovered edge, add both endpoints to the cover.";
        if (frame <= picked.size()) {
            int[] e = picked.get(frame - 1);
            Set<Integer> cover = new HashSet<>();
            for (int i = 0; i < frame; i++) {
                cover.add(picked.get(i)[0]);
                cover.add(picked.get(i)[1]);
            }
            long uncovered = g.edges().stream().filter(x -> !cover.contains(x[0]) && !cover.contains(x[1])).count();
            return "Pick edge " + Math.min(e[0], e[1]) + "-" + Math.max(e[0], e[1]) + ": both endpoints join the cover. Uncovered edges left: " + uncovered + ".";
        }
        return "Every edge is covered. Cover size " + approx.size() + " vs. optimal " + optimal.size() + " (purple rings mark an optimal cover): within the factor 2 guarantee.";
    }
}
