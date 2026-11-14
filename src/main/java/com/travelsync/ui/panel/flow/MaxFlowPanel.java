package com.travelsync.ui.panel.flow;

import com.travelsync.algorithms.flow.DinicSolver;
import com.travelsync.algorithms.flow.EdmondsKarpSolver;
import com.travelsync.algorithms.flow.FlowTrace;
import com.travelsync.algorithms.flow.FordFulkersonSolver;
import com.travelsync.ui.Execution;
import com.travelsync.ui.Ui;
import com.travelsync.util.ValidationException;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.function.Supplier;

/** Max-flow panel: Ford-Fulkerson, Edmonds-Karp or Dinic pushing passengers through the transport network. */
public final class MaxFlowPanel extends FlowPanelBase {

    private static final String FF = "Ford-Fulkerson";
    private static final String EK = "Edmonds-Karp";
    private static final String DINIC = "Dinic";

    private final ComboBox<String> algorithm = new ComboBox<>();

    public MaxFlowPanel() {
        algorithm.getItems().addAll(FF, EK, DINIC);
        algorithm.setValue(FF);
        HBox algoRow = Ui.row(10, Ui.label("Algorithm", "kv-key"), algorithm);
        assemble("Maximum flow \u2013 Ford-Fulkerson, Edmonds-Karp, Dinic",
                "Travel problem: how many passengers can move from one destination to another when every transport route has limited free "
                        + "seats? Each step finds an augmenting path with spare capacity and pushes flow along it; edge labels show flow/capacity. "
                        + "Ford-Fulkerson picks any path (DFS), Edmonds-Karp the shortest (BFS), Dinic saturates a whole level graph per phase.",
                flowInputs(algoRow), graph);
        loadDemo();
    }

    @Override
    public void prefer(String name) {
        if (FF.equals(name) || EK.equals(name) || DINIC.equals(name)) algorithm.setValue(name);
    }

    @Override
    protected void loadDemo() {
        loadFlowDemo();
    }

    @Override
    protected Supplier<Execution> prepare() throws ValidationException {
        Supplier<FlowNetwork> factory = networkFactory();
        String name = algorithm.getValue();
        return () -> execute(factory, name);
    }

    private Execution execute(Supplier<FlowNetwork> factory, String name) {
        FlowNetwork net = factory.get();
        FlowTrace.Result trace = switch (name) {
            case EK -> EdmondsKarpSolver.solveTraced(net.graph, net.source, net.sink);
            case DINIC -> DinicSolver.solveTraced(net.graph, net.source, net.sink);
            default -> FordFulkersonSolver.solveTraced(net.graph, net.source, net.sink);
        };
        FlowNetwork n1 = factory.get();
        FlowNetwork n2 = factory.get();
        FlowNetwork n3 = factory.get();
        long ff = FordFulkersonSolver.solve(n1.graph, n1.source, n1.sink);
        long ek = EdmondsKarpSolver.solve(n2.graph, n2.source, n2.sink);
        long dn = DinicSolver.solve(n3.graph, n3.source, n3.sink);
        List<FlowTrace.Augmentation> augs = trace.augmentations();
        int frames = augs.size() + 1;
        String result = "Maximum flow from " + net.names[net.source] + " to " + net.names[net.sink] + " = " + trace.maxFlow()
                + " passengers, reached with " + augs.size() + " augmenting path(s).";
        String complexity = switch (name) {
            case EK -> "Edmonds-Karp: Time O(V\u00B7E\u00B2) = O(" + net.graph.size() + "\u00B7" + net.forwardEdges.size() + "\u00B2)   |   Space O(V + E)";
            case DINIC -> "Dinic: Time O(V\u00B2\u00B7E) = O(" + net.graph.size() + "\u00B2\u00B7" + net.forwardEdges.size() + ")   |   Space O(V + E)";
            default -> "Ford-Fulkerson: Time O(E\u00B7f) = O(" + net.forwardEdges.size() + "\u00B7" + trace.maxFlow() + ")   |   Space O(V + E)";
        };
        String info = "Cross-check on fresh copies: Ford-Fulkerson " + ff + ", Edmonds-Karp " + ek + ", Dinic " + dn
                + (ff == ek && ek == dn ? " \u2013 all agree." : " \u2013 MISMATCH.");
        return new Execution(frames, f -> net.render(graph, net.flowsAfter(augs, f), f == 0 ? null : augs.get(f - 1), null, null),
                f -> narrate(net, augs, name, f), result, complexity, -1, augs.size(), info);
    }

    private static String narrate(FlowNetwork net, List<FlowTrace.Augmentation> augs, String name, int frame) {
        if (frame == 0) return "The network starts with zero flow. Edge labels read flow/capacity; the ringed nodes are source and sink.";
        FlowTrace.Augmentation a = augs.get(frame - 1);
        String how = switch (name) {
            case EK -> "BFS found a shortest path (" + a.edges().size() + " edges): ";
            case DINIC -> "Phase " + a.phase() + ", blocking flow in the level graph: ";
            default -> "DFS found an augmenting path: ";
        };
        String text = how + net.pathText(a) + ". Bottleneck " + a.bottleneck() + ", total flow now " + a.totalFlow() + ".";
        if (net.usesReverseEdge(a)) text += " It uses a reverse edge, i.e. it re-routes flow pushed earlier.";
        if (frame == augs.size()) text += " No augmenting path remains, so the flow is maximum.";
        return text;
    }
}
