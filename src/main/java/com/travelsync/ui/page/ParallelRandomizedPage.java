package com.travelsync.ui.page;

import com.travelsync.ui.TabbedAlgorithmPage;
import com.travelsync.ui.panel.parallel.MillerRabinPanel;
import com.travelsync.ui.panel.parallel.RandomizedQuickSortPanel;
import com.travelsync.ui.panel.parallel.ReservoirSamplingPanel;
import com.travelsync.ui.panel.parallel.ScanReduceBrentPanel;

/** M6 page: randomized and parallel algorithms applied to ranking, sampling, identifiers and demand analytics. */
public final class ParallelRandomizedPage extends TabbedAlgorithmPage {

    public ParallelRandomizedPage() {
        addPanel("QuickSort", new RandomizedQuickSortPanel(), "Randomized QuickSort");
        addPanel("Reservoir Sampling", new ReservoirSamplingPanel(), "Reservoir Sampling");
        addPanel("Miller-Rabin", new MillerRabinPanel(), "Miller-Rabin");
        addPanel("Scan / Reduce / Brent", new ScanReduceBrentPanel(), "Blelloch Scan", "Parallel Reduce", "Brent's Theorem");
    }

    @Override
    public String title() { return "Parallel & Randomized"; }

    @Override
    public String subtitle() { return "M6 \u2013 QuickSort, reservoir sampling, Miller-Rabin, Blelloch scan, parallel reduce, Brent's theorem"; }
}
