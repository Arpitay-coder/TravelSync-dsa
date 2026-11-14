package com.travelsync.ui.page;

import com.travelsync.ui.TabbedAlgorithmPage;
import com.travelsync.ui.panel.flow.BipartiteMatchingPanel;
import com.travelsync.ui.panel.flow.MaxFlowPanel;
import com.travelsync.ui.panel.flow.MinCutPanel;

/** M4 page: network flow applied to transport capacity, bottlenecks and traveler-hotel assignment. */
public final class NetworkFlowPage extends TabbedAlgorithmPage {

    public NetworkFlowPage() {
        addPanel("Max Flow", new MaxFlowPanel(), "Ford-Fulkerson", "Edmonds-Karp", "Dinic");
        addPanel("Min-Cut", new MinCutPanel(), "Min-Cut");
        addPanel("Bipartite Matching", new BipartiteMatchingPanel(), "Bipartite Matching");
    }

    @Override
    public String title() { return "Network Flow"; }

    @Override
    public String subtitle() { return "M4 \u2013 Ford-Fulkerson, Edmonds-Karp, Dinic, min-cut and bipartite matching"; }
}
