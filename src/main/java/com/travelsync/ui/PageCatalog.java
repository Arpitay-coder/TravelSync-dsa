package com.travelsync.ui;

import com.travelsync.controller.NavigationController;
import com.travelsync.controller.NavigationController.Route;
import com.travelsync.ui.page.AboutPage;
import com.travelsync.ui.page.AlgorithmExplorerPage;
import com.travelsync.ui.page.AnalyticsPage;
import com.travelsync.ui.page.ComplexityLabPage;
import com.travelsync.ui.page.DashboardPage;
import com.travelsync.ui.page.DemoModePage;
import com.travelsync.ui.page.DocumentIntelligencePage;
import com.travelsync.ui.page.NetworkFlowPage;
import com.travelsync.ui.page.OptimizationLabPage;
import com.travelsync.ui.page.ParallelRandomizedPage;
import com.travelsync.ui.page.StringIntelligencePage;
import com.travelsync.ui.page.ViewPage;
import com.travelsync.ui.view.DestinationsView;

import java.util.List;

/** Builds the sidebar routes and the pages behind them. */
public final class PageCatalog {

    private PageCatalog() { }

    public static List<Route> routes(NavigationController nav) {
        return List.of(
                new Route("\uD83C\uDFE0", "Dashboard", () -> new DashboardPage(nav)),
                new Route("\uD83D\uDCCD", "Destinations", () -> new ViewPage("Destinations",
                        "Searchable, sortable destination catalogue", DestinationsView::new)),
                new Route("\uD83D\uDD0D", "String Intelligence", StringIntelligencePage::new),
                new Route("\uD83D\uDCC4", "Document Intelligence", DocumentIntelligencePage::new),
                new Route("\uD83E\uDDEE", "Optimization Lab", OptimizationLabPage::new),
                new Route("\uD83D\uDEE3", "Network Flow", NetworkFlowPage::new),
                new Route("\uD83E\uDDE9", "Complexity Lab", ComplexityLabPage::new),
                new Route("\u26A1", "Parallel & Randomized", ParallelRandomizedPage::new),
                new Route("\uD83D\uDCCA", "Analytics", AnalyticsPage::new),
                new Route("\uD83E\uDDED", "Algorithm Explorer", () -> new AlgorithmExplorerPage(nav)),
                new Route("\uD83C\uDF9E", "Presentation Demo", () -> new DemoModePage(nav)),
                new Route("\u2139", "About Project", AboutPage::new));
    }
}
