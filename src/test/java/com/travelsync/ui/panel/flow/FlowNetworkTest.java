package com.travelsync.ui.panel.flow;

import com.travelsync.algorithms.flow.DinicSolver;
import com.travelsync.algorithms.flow.FlowGraph;
import com.travelsync.algorithms.flow.FlowTrace;
import com.travelsync.data.AppDataStore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlowNetworkTest {

    @Test
    void replayedFlowsMatchTheFinalResidualGraph() {
        FlowNetwork net = FlowNetwork.textbook();
        FlowTrace.Result r = DinicSolver.solveTraced(net.graph, net.source, net.sink);
        long[] replayed = net.flowsAfter(r.augmentations(), r.augmentations().size());
        for (int i = 0; i < replayed.length; i++) {
            assertEquals(FlowGraph.flowOn(net.forwardEdges.get(i)), replayed[i], "edge " + i);
        }
    }

    @Test
    void noFlowBeforeTheFirstAugmentation() {
        FlowNetwork net = FlowNetwork.textbook();
        FlowTrace.Result r = DinicSolver.solveTraced(net.graph, net.source, net.sink);
        for (long f : net.flowsAfter(r.augmentations(), 0)) assertEquals(0, f);
    }

    @Test
    void liveNetworkFromSampleDataHasPositiveMaxFlowBetweenFirstAndLastDestination() {
        AppDataStore store = AppDataStore.getInstance();
        FlowNetwork net = FlowNetwork.live(store.getDestinations(), store.getTransportation(), 0, store.getDestinations().size() - 1);
        assertTrue(DinicSolver.solve(net.graph, net.source, net.sink) > 0);
    }
}
