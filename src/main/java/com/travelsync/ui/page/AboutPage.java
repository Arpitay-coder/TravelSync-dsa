package com.travelsync.ui.page;

import com.travelsync.ui.AlgorithmCatalog;
import com.travelsync.ui.Page;
import com.travelsync.ui.Ui;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** About Project: fixed content describing TravelSync for the DSA problem-based learning submission. */
public final class AboutPage implements Page {

    @Override
    public String title() { return "About Project"; }

    @Override
    public String subtitle() { return "TravelSync \u2014 DSA Problem-Based Learning project"; }

    @Override
    public Node content() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(4, 24, 24, 24));

        root.getChildren().add(Ui.card(
                Ui.label("TRAVELSYNC", "page-title-lg"),
                Ui.label("Intelligent Travel Planning & Information Platform", "subtle-text"),
                wrapped("TravelSync models a travel-management platform: destinations and attractions, hotels and transportation, "
                        + "travelers and travel requests, booking records and travel documents, and available travel resources and "
                        + "continuous booking activity. It supports fast destination/document search, itinerary optimisation, name "
                        + "correction, resource allocation, network-flow analysis, travel-constraint analysis and algorithm visualisation.")));

        VBox modules = Ui.card(Ui.label("MODULES", "section-title"));
        modules.getChildren().add(wrapped("M1  String Algorithms \u2014 KMP, Z-Function, Rabin-Karp, Aho-Corasick (String Intelligence)"));
        modules.getChildren().add(wrapped("M2  Suffix Structures \u2014 Suffix Array, SA-IS, LCP/Kasai, Suffix Automaton (Document Intelligence)"));
        modules.getChildren().add(wrapped("M3  Advanced Dynamic Programming \u2014 Levenshtein/Damerau, Bitmask DP, Matrix Chain, Optimal BST (Optimization Lab)"));
        modules.getChildren().add(wrapped("M4  Network Flow \u2014 Ford-Fulkerson, Edmonds-Karp, Dinic, Bipartite Matching, Minimum Cut (Network Flow)"));
        modules.getChildren().add(wrapped("M5  NP-Completeness & Approximation \u2014 SAT/3-SAT, 3-SAT\u2192CLIQUE, CLIQUE\u2192IND.SET, IND.SET\u2192VERTEX-COVER, Vertex-Cover 2-Approximation (Complexity Lab)"));
        modules.getChildren().add(wrapped("M6  Randomized & Parallel Algorithms \u2014 Randomized QuickSort, Reservoir Sampling, Miller-Rabin, Blelloch Scan, Parallel Reduce, Brent's Theorem (Parallel & Randomized)"));
        root.getChildren().add(modules);

        root.getChildren().add(Ui.card(Ui.label("HOW DSA SOLVES REAL TRAVEL PROBLEMS", "section-title"),
                wrapped("Every module in TravelSync answers a concrete operational question with a specific data structure or "
                        + "algorithm rather than a generic database query:"),
                wrapped("\u2022 Finding a destination, hotel name or booking code among thousands of records needs sub-linear, "
                        + "provably-correct pattern matching (M1) instead of naive string scanning."),
                wrapped("\u2022 Indexing free-text travel descriptions for instant substring search needs a structure built once and "
                        + "queried in O(m) (M2), not a re-scan on every query."),
                wrapped("\u2022 Name typos and short-itinerary planning are optimisation problems with overlapping subproblems, solved "
                        + "once by dynamic programming instead of exponential brute force (M3)."),
                wrapped("\u2022 How many travelers can move from one destination to another, and where the bottleneck route is, is "
                        + "exactly the max-flow / min-cut theorem (M4)."),
                wrapped("\u2022 Some constraints (traveler preferences, destination compatibility) are inherently exponential to solve "
                        + "exactly; understanding NP-completeness explains why only approximate or restricted solutions scale, and the "
                        + "2-approximation for vertex cover still gives a usable, provably-bounded answer (M5)."),
                wrapped("\u2022 At real-world data volumes, a single CPU core is not enough: randomised algorithms give predictable "
                        + "average-case performance and parallel primitives (scan, reduce) turn multiple cores into actual throughput, "
                        + "with Brent's theorem explaining exactly how many cores are worth using (M6).")));

        root.getChildren().add(Ui.card(Ui.label("PROJECT INFO", "section-title"),
                wrapped("Technology: Java 17, JavaFX 21, Maven, JUnit 5. No Spring Boot, no web framework \u2014 a native desktop application."),
                wrapped("Every core DSA algorithm is implemented by hand in algorithms/ (never a one-line library call); "
                        + AlgorithmCatalog.entries().size() + " algorithms are catalogued in the Algorithm Explorer, each runnable and visualised."),
                wrapped("Light and dark themes are both fully supported and can be switched from the top bar at any time."),
                wrapped("Team contribution: fill in the README's contribution template with each member's module(s).")));

        return root;
    }

    private static Label wrapped(String text) {
        Label l = Ui.label(text, "subtle-text");
        l.setWrapText(true);
        return l;
    }
}
