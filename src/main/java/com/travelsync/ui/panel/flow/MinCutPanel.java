package com.travelsync.ui.panel.flow;

import com.travelsync.algorithms.flow.DinicSolver;
import com.travelsync.algorithms.flow.FlowGraph;
import com.travelsync.algorithms.flow.FlowTrace;
import com.travelsync.algorithms.flow.MinCutSolver;
import com.travelsync.ui.Execution;
import com.travelsync.util.ValidationException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/** Min-cut panel: after Dinic finishes, the saturated routes between source side and sink side are the bottleneck. */
public final class MinCutPanel extends FlowPanelBase {

    public MinCutPanel() {
        assemble("Minimum cut \u2013 transportation bottleneck",
                "Travel problem: which routes limit the whole network? By the max-flow min-cut theorem the smallest set of routes whose "
                        + "removal disconnects source from sink has exactly the capacity of the maximum flow. After the flow is maximal, the "
                        + "nodes still reachable from the source through unsaturated edges form the source side; edges leaving it are the cut.",
                flowInputs(), graph);
        loadDemo();
    }

    @Override
    protected void loadDemo() {
        loadFlowDemo();
    }

    @Override
    protected Supplier<Execution> prepare() throws ValidationException {
        Supplier<FlowNetwork> factory = networkFactory();
        return () -> execute(factory);
    }

    private Execution execute(Supplier<FlowNetwork> factory) {
        FlowNetwork net = factory.get();
        FlowTrace.Result trace = DinicSolver.solveTraced(net.graph, net.source, net.sink);
        Set<Integer> sourceSide = reachable(net);
        List<FlowGraph.Edge> cut = new ArrayList<>();
        long capacity = 0;
        StringBuilder names = new StringBuilder();
        for (FlowGraph.Edge e : net.forwardEdges) {
            if (sourceSide.contains(e.from) && !sourceSide.contains(e.to)) {
                cut.add(e);
                capacity += e.originalCapacity;
                names.append(names.length() == 0 ? "" : ", ").append(net.names[e.from]).append(" \u2192 ").append(net.names[e.to]);
            }
        }
        FlowNetwork fresh = factory.get();
        MinCutSolver.Result check = MinCutSolver.solve(fresh.graph, fresh.source, fresh.sink);
        List<FlowTrace.Augmentation> augs = trace.augmentations();
        int frames = augs.size() + 2; // initial + one per augmentation + final cut view
        String result = "Minimum cut capacity = " + capacity + " = maximum flow " + trace.maxFlow() + "\nBottleneck route(s): "
                + (cut.isEmpty() ? "none \u2013 source and sink are not connected" : names);
        String info = "MinCutSolver cross-check: cut value " + check.minCutValue() + " with " + check.cutEdges().size() + " cut edge(s) \u2013 "
                + (check.minCutValue() == capacity ? "matches." : "MISMATCH.");
        long[] finalFlows = net.flowsAfter(augs, augs.size());
        return new Execution(frames, f -> {
            if (f <= augs.size()) net.render(graph, net.flowsAfter(augs, f), f == 0 ? null : augs.get(f - 1), null, null);
            else net.render(graph, finalFlows, null, sourceSide, cut);
        }, f -> f <= augs.size()
                ? (f == 0 ? "Dinic first computes the maximum flow; each frame is one augmentation."
                : "Augmenting " + net.pathText(augs.get(f - 1)) + " (+" + augs.get(f - 1).bottleneck() + "), total " + augs.get(f - 1).totalFlow() + ".")
                : "Max flow reached. Blue nodes can still be reached from the source; the red edges leaving them are saturated and form the minimum cut.",
                result, "Dinic O(V\u00B2\u00B7E) + one BFS O(V + E) on the residual graph", -1, augs.size(), info);
    }

    private static Set<Integer> reachable(FlowNetwork net) {
        Set<Integer> seen = new HashSet<>();
        Deque<Integer> queue = new ArrayDeque<>();
        queue.add(net.source);
        seen.add(net.source);
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (FlowGraph.Edge e : net.graph.adj(u)) {
                if (e.capacity > 0 && seen.add(e.to)) queue.add(e.to);
            }
        }
        return seen;
    }
}
