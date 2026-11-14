package com.travelsync.ui.page;

import com.travelsync.ui.TabbedAlgorithmPage;
import com.travelsync.ui.panel.complexity.IndependentSetVertexCoverPanel;
import com.travelsync.ui.panel.complexity.ThreeSatCliquePanel;
import com.travelsync.ui.panel.complexity.ThreeSatPanel;
import com.travelsync.ui.panel.complexity.VertexCoverApproxPanel;

/** M5 page: NP-completeness reductions and approximation on travel constraints. */
public final class ComplexityLabPage extends TabbedAlgorithmPage {

    public ComplexityLabPage() {
        addPanel("3-SAT", new ThreeSatPanel(), "3-SAT");
        addPanel("3-SAT \u2192 CLIQUE", new ThreeSatCliquePanel(), "3-SAT to CLIQUE");
        addPanel("Independent Set & Vertex Cover", new IndependentSetVertexCoverPanel(), "CLIQUE to INDEPENDENT SET", "INDEPENDENT SET to VERTEX COVER");
        addPanel("2-Approximation", new VertexCoverApproxPanel(), "Vertex Cover 2-Approximation");
    }

    @Override
    public String title() { return "Complexity Lab"; }

    @Override
    public String subtitle() { return "M5 \u2013 3-SAT, CLIQUE, INDEPENDENT SET, VERTEX COVER and the 2-approximation"; }
}
