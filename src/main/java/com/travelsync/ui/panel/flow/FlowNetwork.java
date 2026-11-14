package com.travelsync.ui.panel.flow;

import com.travelsync.algorithms.flow.FlowGraph;
import com.travelsync.algorithms.flow.FlowTrace;
import com.travelsync.model.Destination;
import com.travelsync.model.Transportation;
import com.travelsync.visualization.CellState;
import com.travelsync.visualization.GraphLayouts;
import com.travelsync.visualization.GraphVisualizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** A flow network together with node names, layout and drawing logic; built from live transport data or the textbook example. */
final class FlowNetwork {

    static final double W = 700;
    static final double H = 400;

    final FlowGraph graph;
    final String[] names;
    final double[][] positions;
    final int source;
    final int sink;
    final boolean textbook;
    final List<FlowGraph.Edge> forwardEdges = new ArrayList<>();

    private FlowNetwork(FlowGraph graph, String[] names, double[][] positions, int source, int sink, boolean textbook) {
        this.graph = graph;
        this.names = names;
        this.positions = positions;
        this.source = source;
        this.sink = sink;
        this.textbook = textbook;
        for (int u = 0; u < graph.size(); u++) {
            for (FlowGraph.Edge e : graph.adj(u)) {
                if (e.isForward) forwardEdges.add(e);
            }
        }
    }

    /** The classic six-node example whose maximum flow is 23. */
    static FlowNetwork textbook() {
        FlowGraph g = new FlowGraph(6);
        g.addEdge(0, 1, 16); g.addEdge(0, 2, 13);
        g.addEdge(1, 2, 10); g.addEdge(2, 1, 4);
        g.addEdge(1, 3, 12); g.addEdge(3, 2, 9);
        g.addEdge(2, 4, 14); g.addEdge(4, 3, 7);
        g.addEdge(3, 5, 20); g.addEdge(4, 5, 4);
        double[][] pos = {{60, 200}, {230, 80}, {230, 320}, {470, 80}, {470, 320}, {640, 200}};
        return new FlowNetwork(g, new String[]{"S", "A", "B", "C", "D", "T"}, pos, 0, 5, true);
    }

    /** Nodes are destinations; an edge's capacity is the seats still available on that transport route. */
    static FlowNetwork live(List<Destination> destinations, List<Transportation> routes, int source, int sink) {
        Map<String, Integer> index = new HashMap<>();
        String[] names = new String[destinations.size()];
        for (int i = 0; i < destinations.size(); i++) {
            index.put(destinations.get(i).getId(), i);
            names[i] = destinations.get(i).getName();
        }
        FlowGraph g = new FlowGraph(destinations.size());
        for (Transportation t : routes) {
            Integer from = index.get(t.getSourceDestinationId());
            Integer to = index.get(t.getTargetDestinationId());
            if (from != null && to != null && t.getAvailableCapacity() > 0) {
                g.addEdge(from, to, t.getAvailableCapacity());
            }
        }
        return new FlowNetwork(g, names, GraphLayouts.circle(names.length, W, H), source, sink, false);
    }

    /** Flow on every forward edge after the first k augmentations (indexed like {@link #forwardEdges}). */
    long[] flowsAfter(List<FlowTrace.Augmentation> augmentations, int k) {
        Map<FlowGraph.Edge, Long> flow = new IdentityHashMap<>();
        for (int a = 0; a < k; a++) {
            FlowTrace.Augmentation aug = augmentations.get(a);
            for (FlowGraph.Edge e : aug.edges()) {
                FlowGraph.Edge forward = e.isForward ? e : graph.adj(e.to).get(e.reverseIndex);
                flow.merge(forward, e.isForward ? aug.bottleneck() : -aug.bottleneck(), Long::sum);
            }
        }
        long[] result = new long[forwardEdges.size()];
        for (int i = 0; i < result.length; i++) result[i] = flow.getOrDefault(forwardEdges.get(i), 0L);
        return result;
    }

    String pathText(FlowTrace.Augmentation aug) {
        StringBuilder sb = new StringBuilder(names[aug.edges().get(0).from]);
        for (FlowGraph.Edge e : aug.edges()) sb.append(" \u2192 ").append(names[e.to]);
        return sb.toString();
    }

    boolean usesReverseEdge(FlowTrace.Augmentation aug) {
        return aug.edges().stream().anyMatch(e -> !e.isForward);
    }

    /**
     * Draws the network.
     *
     * @param highlight  augmenting path to emphasise, or null
     * @param sourceSide nodes to colour as the source side of a cut, or null
     * @param cutEdges   forward edges crossing the cut, or null
     */
    void render(GraphVisualizer viz, long[] flows, FlowTrace.Augmentation highlight, Set<Integer> sourceSide,
                List<FlowGraph.Edge> cutEdges) {
        Set<FlowGraph.Edge> onPath = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<Integer> pathNodes = new java.util.HashSet<>();
        if (highlight != null) {
            for (FlowGraph.Edge e : highlight.edges()) {
                onPath.add(e.isForward ? e : graph.adj(e.to).get(e.reverseIndex));
                pathNodes.add(e.from);
                pathNodes.add(e.to);
            }
        }
        Set<FlowGraph.Edge> cut = Collections.newSetFromMap(new IdentityHashMap<>());
        if (cutEdges != null) cut.addAll(cutEdges);

        List<GraphVisualizer.GNode> nodes = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            String label = textbook ? names[i] : names[i].substring(0, Math.min(3, names[i].length())).toUpperCase();
            GraphVisualizer.GNode n = new GraphVisualizer.GNode(i, label, positions[i][0], positions[i][1]);
            n.sub = textbook ? "" : names[i];
            if (pathNodes.contains(i)) n.state = CellState.ACTIVE;
            if (sourceSide != null) n.state = sourceSide.contains(i) ? CellState.ACTIVE : CellState.NORMAL;
            if (i == source || i == sink) n.ring = CellState.HIGHLIGHT;
            nodes.add(n);
        }
        List<GraphVisualizer.GEdge> edges = new ArrayList<>();
        for (int idx = 0; idx < forwardEdges.size(); idx++) {
            FlowGraph.Edge e = forwardEdges.get(idx);
            GraphVisualizer.GEdge ge = new GraphVisualizer.GEdge(e.from, e.to);
            ge.label = flows[idx] + "/" + e.originalCapacity;
            if (flows[idx] > 0) ge.state = flows[idx] == e.originalCapacity ? CellState.MATCH : CellState.DONE;
            if (onPath.contains(e)) { ge.state = CellState.ACTIVE; ge.width = 3.2; }
            if (cut.contains(e)) { ge.state = CellState.MISMATCH; ge.width = 3.6; }
            edges.add(ge);
        }
        viz.render(nodes, edges);
    }
}
