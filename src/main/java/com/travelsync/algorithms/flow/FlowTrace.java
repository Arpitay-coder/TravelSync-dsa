package com.travelsync.algorithms.flow;

import java.util.List;

/** Result types of the traced max-flow solvers: the flow value and every augmentation actually performed. */
public final class FlowTrace {

    /**
     * One augmentation.
     *
     * @param edges      the edges of the augmenting path in order from the source (residual edges included)
     * @param bottleneck flow pushed along the path
     * @param totalFlow  flow value after this augmentation
     * @param phase      Dinic phase (BFS round) or 0 for the other algorithms
     */
    public record Augmentation(List<FlowGraph.Edge> edges, long bottleneck, long totalFlow, int phase) { }

    public record Result(long maxFlow, List<Augmentation> augmentations) { }

    private FlowTrace() { }
}
