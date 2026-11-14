package com.travelsync.ui;

import java.util.List;

/** Metadata of every implemented algorithm: drives the Algorithm Explorer and the Presentation Demo Mode. */
public final class AlgorithmCatalog {

    /** One algorithm. {@code route} is the sidebar label of the page that hosts it; {@code name} focuses it there. */
    public record Entry(String module, String name, String problem, String input, String output, String time, String space,
                        String explanation, String visualization, String application, String route) {
    }

    public static final String M1 = "M1 \u2013 String Algorithms";
    public static final String M2 = "M2 \u2013 Suffix Structures";
    public static final String M3 = "M3 \u2013 Dynamic Programming";
    public static final String M4 = "M4 \u2013 Network Flow";
    public static final String M5 = "M5 \u2013 NP-Completeness";
    public static final String M6 = "M6 \u2013 Randomized & Parallel";

    private static final List<Entry> ENTRIES = List.of(
            new Entry(M1, "KMP", "Destination / hotel name search", "Text, pattern", "Match positions, LPS table", "O(n + m)", "O(m)",
                    "Precomputes the longest-proper-prefix-that-is-also-a-suffix table so the pattern pointer falls back inside the pattern and the text pointer never moves backwards.",
                    "Text and pattern rows with the compared cells, LPS table and fallback jumps", "Find a destination or hotel name inside a large catalogue", "String Intelligence"),
            new Entry(M1, "Z-Function", "Repeated-phrase detection", "String (and optional phrase)", "Z array, occurrences", "O(n)", "O(n)",
                    "z[i] is the longest substring at i equal to a prefix; the Z-box reuses earlier work so each character is matched once.",
                    "Character row, Z-box highlight and the Z values as they are computed", "Detect a repeated phrase across destination descriptions", "String Intelligence"),
            new Entry(M1, "Rabin-Karp", "Booking-code search", "Text, pattern", "Match positions, hash statistics", "O(n + m) expected", "O(1)",
                    "A rolling polynomial hash of the window is updated in O(1); characters are compared only when hashes are equal (spurious hits are caught).",
                    "Sliding window with window hash versus pattern hash and verification result", "Search booking / reservation codes efficiently", "String Intelligence"),
            new Entry(M1, "Aho-Corasick", "Multi-keyword search", "Several keywords, text", "All occurrences of all keywords", "O(n + m + z)", "O(m\u00B7\u03C3)",
                    "Builds a trie of all keywords, adds failure links with BFS, then scans the text once, following links on mismatches.",
                    "Trie automaton state and highlighted matches as the text is read", "Detect many destination/service keywords in a traveler's request", "String Intelligence"),
            new Entry(M2, "Suffix Array", "Document indexing", "Document text", "Sorted suffixes", "O(n log n)", "O(n)",
                    "Sorts all suffixes by prefix-doubling ranks so any substring query reduces to binary search over the sorted order.",
                    "Table of sorted suffixes, rank by rank", "Index hotel and destination descriptions for fast substring search", "Document Intelligence"),
            new Entry(M2, "SA-IS", "Linear-time suffix array", "Document text", "Suffix array", "O(n)", "O(n)",
                    "Classifies suffixes as S/L, sorts LMS suffixes recursively on a reduced string, then induces all others with two linear scans.",
                    "Same sorted-suffix table, cross-validated against the doubling method", "Large-scale travel-document indexing", "Document Intelligence"),
            new Entry(M2, "LCP / Kasai", "Repeated phrase detection", "Document text", "LCP array, longest repeat", "O(n)", "O(n)",
                    "Computes the longest common prefix of neighbouring suffixes in text order, reusing h \u2212 1 from the previous step.",
                    "Suffix table with LCP values filled in, longest repeat highlighted", "Find common phrases shared between destination descriptions", "Document Intelligence"),
            new Entry(M2, "Suffix Automaton", "Substring & similarity search", "Two texts", "Longest common substring", "Build O(n)", "O(n)",
                    "Builds the minimal automaton accepting every substring of the source text; walking the second text finds shared phrases via suffix links.",
                    "Match length per character as the second text is walked, with fallbacks", "Compare two travel descriptions for shared content", "Document Intelligence"),
            new Entry(M3, "Edit Distance", "Name correction", "Two strings", "Edit distance, edit script", "O(m\u00B7n)", "O(m\u00B7n)",
                    "DP over prefixes: a cell is the cheapest of insert, delete, substitute (or copy on equal letters); traceback gives the edit script.",
                    "Edit-distance matrix filled cell by cell with the optimal path highlighted", "Correct a misspelt destination or hotel name", "Optimization Lab"),
            new Entry(M3, "Damerau-Levenshtein", "Name correction with swaps", "Two strings", "Edit distance incl. transpositions", "O(m\u00B7n)", "O(m\u00B7n)",
                    "Levenshtein plus one extra transition: swapping two adjacent letters costs 1.",
                    "Same matrix with the transposition transition highlighted", "Typos such as \"Barcleona\" for \"Barcelona\"", "Optimization Lab"),
            new Entry(M3, "Bitmask DP", "Short itinerary optimisation", "Stops with coordinates", "Optimal visiting order and cost", "O(2\u207F\u00B7n\u00B2)", "O(2\u207F\u00B7n)",
                    "dp[mask][last] is the cheapest way to visit the stops in mask ending at last (Held-Karp), using real great-circle distances.",
                    "Route drawn leg by leg on a map of the chosen destinations", "Plan the cheapest order for a multi-stop trip", "Optimization Lab"),
            new Entry(M3, "Matrix Chain", "Sequencing travel-data operations", "Dimension list", "Minimum cost and parenthesisation", "O(n\u00B3)", "O(n\u00B2)",
                    "cost[i][j] = min over split k of cost[i][k] + cost[k+1][j] + p(i\u22121)\u00B7p(k)\u00B7p(j).",
                    "Triangular cost matrix filled by chain length, best split highlighted", "Order a chain of booking/demand matrix operations cheaply", "Optimization Lab"),
            new Entry(M3, "Optimal BST", "Frequently searched destinations", "Keys with search frequencies", "Minimum expected cost and tree", "O(n\u00B3)", "O(n\u00B2)",
                    "cost[i][j] = \u03A3 freq + min over root r of cost[i][r\u22121] + cost[r+1][j].",
                    "Tree built node by node, compared against a plain alphabetical chain", "Arrange popular destinations for fastest average lookup", "Optimization Lab"),
            new Entry(M4, "Ford-Fulkerson", "Transport-capacity flow", "Capacity network, source, sink", "Maximum flow", "O(E\u00B7|f*|)", "O(V + E)",
                    "Find any augmenting path in the residual graph (DFS) and push its bottleneck; repeat until none exists.",
                    "Graph with the augmenting path and flow/capacity labels", "How many passengers can move between two destinations", "Network Flow"),
            new Entry(M4, "Edmonds-Karp", "Capacity-based transport networks", "Capacity network, source, sink", "Maximum flow", "O(V\u00B7E\u00B2)", "O(V + E)",
                    "Ford-Fulkerson with shortest (BFS) augmenting paths, independent of capacities.",
                    "Same graph, augmenting paths animated in shortest-path order", "Passenger-throughput planning", "Network Flow"),
            new Entry(M4, "Dinic", "Large transportation networks", "Capacity network, source, sink", "Maximum flow, phases", "O(V\u00B2\u00B7E)", "O(V + E)",
                    "Builds a BFS level graph, saturates it with a blocking flow, and repeats with longer shortest paths.",
                    "Level-graph phases and the blocking-flow paths per phase", "Large-scale transport network planning", "Network Flow"),
            new Entry(M4, "Min-Cut", "Transportation bottlenecks", "Capacity network, source, sink", "Cut edges and capacity", "O(V + E) after max flow", "O(V)",
                    "After max flow, nodes reachable in the residual graph form the source side; saturated edges leaving it form the minimum cut.",
                    "Cut edges in red, source side highlighted", "Identify which routes limit the whole network", "Network Flow"),
            new Entry(M4, "Bipartite Matching", "Traveler\u2013hotel assignment", "Travelers, hotels, eligibility", "Maximum assignment", "O(V\u00B7E)", "O(V)",
                    "For each traveler search an augmenting path; a hotel that is taken may be reassigned along the path.",
                    "Travelers left, hotels right, matched edges highlighted", "Assign travelers to hotels matching their preferences", "Network Flow"),
            new Entry(M5, "3-SAT", "Travel-constraint satisfiability", "CNF clauses", "Satisfying assignment or UNSAT", "Search O(2\u207F\u00B7m)", "O(n)",
                    "Tries assignments in order, checking every clause (the NP certificate check itself is O(m)).",
                    "Clause table coloured true/false per assignment", "Check whether a traveler's requirements can all be met", "Complexity Lab"),
            new Entry(M5, "3-SAT to CLIQUE", "Demonstrating NP-hardness", "3-CNF formula", "Reduction graph, clique, assignment", "Reduction O(m\u00B2)", "O(m\u00B2)",
                    "One vertex per literal occurrence; join literals of different clauses that do not contradict; an m-clique means satisfiable.",
                    "Clause columns, compatibility edges and the found clique", "Show that CLIQUE is as hard as 3-SAT", "Complexity Lab"),
            new Entry(M5, "CLIQUE to INDEPENDENT SET", "Destination-conflict modelling", "Graph", "Complement graph, independent set", "O(n\u00B2)", "O(n\u00B2)",
                    "A clique of G is exactly an independent set of the complement graph.",
                    "Conflict graph and its complement, same vertices highlighted", "Find destinations that can be combined in one trip", "Complexity Lab"),
            new Entry(M5, "INDEPENDENT SET to VERTEX COVER", "Destination-conflict modelling", "Graph", "Vertex cover = V \\ independent set", "O(n)", "O(n)",
                    "S is independent iff V \\ S covers every edge, so |S| + |cover| = n.",
                    "Independent set, then its complement covering every conflict edge", "Choosing a conflict-free set of destinations to keep", "Complexity Lab"),
            new Entry(M5, "Vertex Cover 2-Approximation", "Transportation hub selection", "Graph", "Cover within factor 2 of optimal", "O(V + E)", "O(V)",
                    "Repeatedly take an uncovered edge and add both endpoints; the chosen edges form a matching so the cover is \u2264 2\u00B7OPT.",
                    "Picked edges, covered vertices, compared with the true optimum", "Pick a small set of hub destinations covering every route", "Complexity Lab"),
            new Entry(M6, "Randomized QuickSort", "Destination ranking", "Preference scores", "Sorted ranking, comparison count", "Expected O(n log n)", "O(log n)",
                    "Partition around a uniformly random pivot and recurse on both sides.",
                    "Array with pivot, scan pointer, partition regions and fixed positions", "Rank destinations by a traveler's preference score", "Parallel & Randomized"),
            new Entry(M6, "Reservoir Sampling", "Continuous booking streams", "Stream length, sample size", "Uniform random sample", "O(N)", "O(k)",
                    "Keep the first k; replace a random slot with probability k/i for item i.",
                    "Reservoir slots changing as bookings arrive", "Fair sample of an endless booking stream", "Parallel & Randomized"),
            new Entry(M6, "Miller-Rabin", "Large numeric identifiers", "Integer, rounds", "Prime / composite, witness", "O(k\u00B7log\u00B3 n)", "O(log n)",
                    "Tests whether a random base is a witness of compositeness; error \u2264 4\u207B\u1D4F for composites, none for primes.",
                    "Round table with the squaring sequence and verdict", "Primality checks for large booking/capacity identifiers", "Parallel & Randomized"),
            new Entry(M6, "Blelloch Scan", "Cumulative booking statistics", "Numbers", "Exclusive prefix sums", "Work O(n), depth O(log n)", "O(n)",
                    "Up-sweep builds partial sums in a binary tree, the root is cleared, the down-sweep pushes prefixes back down.",
                    "Array after each up-sweep/down-sweep round, combined pairs highlighted", "Running totals over daily bookings", "Parallel & Randomized"),
            new Entry(M6, "Parallel Reduce", "Travel-demand aggregation", "Large array", "Aggregate, measured timings", "Work O(n), depth O(log n)", "O(P)",
                    "Chunks are reduced by Java's Fork/Join worker threads and the partial results combined.",
                    "Sequential reduction tree, plus measured single-thread vs. parallel timings", "Aggregate millions of demand records", "Parallel & Randomized"),
            new Entry(M6, "Brent's Theorem", "Evaluating parallel processing", "Work W, depth D, processors P", "Time bounds, speed-up", "T_P \u2264 W/P + D", "\u2014",
                    "A greedy schedule needs at most W/P + D; speed-up is capped by W/D.",
                    "Bound computed from the measured work/depth of the reduce above", "How many processors are worth using for demand analytics", "Parallel & Randomized"));

    private AlgorithmCatalog() { }

    public static List<Entry> entries() { return ENTRIES; }

    /** @throws IllegalArgumentException if unknown */
    public static Entry byName(String name) {
        return ENTRIES.stream().filter(e -> e.name().equals(name)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown algorithm: " + name));
    }

    /** Ten algorithms across all six modules, in a sensible classroom presentation order. */
    public static List<String> demoSequence() {
        return List.of("KMP", "Aho-Corasick", "Suffix Automaton", "Edit Distance", "Bitmask DP", "Dinic",
                "Bipartite Matching", "Vertex Cover 2-Approximation", "Randomized QuickSort", "Parallel Reduce");
    }
}
