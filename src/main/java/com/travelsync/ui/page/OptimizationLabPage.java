package com.travelsync.ui.page;

import com.travelsync.ui.TabbedAlgorithmPage;
import com.travelsync.ui.panel.dp.BitmaskItineraryPanel;
import com.travelsync.ui.panel.dp.EditDistancePanel;
import com.travelsync.ui.panel.dp.MatrixChainPanel;
import com.travelsync.ui.panel.dp.OptimalBstPanel;

/** M3 page: dynamic programming for name correction, itineraries, analytics chains and search layout. */
public final class OptimizationLabPage extends TabbedAlgorithmPage {

    public OptimizationLabPage() {
        addPanel("Edit Distance", new EditDistancePanel(), "Edit Distance", "Damerau-Levenshtein");
        addPanel("Bitmask DP", new BitmaskItineraryPanel(), "Bitmask DP");
        addPanel("Matrix Chain", new MatrixChainPanel(), "Matrix Chain");
        addPanel("Optimal BST", new OptimalBstPanel(), "Optimal BST");
    }

    @Override
    public String title() { return "Optimization Lab"; }

    @Override
    public String subtitle() { return "M3 \u2013 Levenshtein/Damerau, bitmask itinerary DP, matrix chain and optimal BST"; }
}
