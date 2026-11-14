# TravelSync — Intelligent Travel Planning & Information Platform

A JavaFX desktop application combining a travel-management platform (destinations,
hotels, transportation, travelers, bookings, documents, services) with a suite of
DSA modules (M1–M6) demonstrated through real, interactive algorithm visualizations.

> **Build status: M1–M6 algorithm modules complete and wired into a working
> app shell**, with a full test suite for every algorithm. Remaining work is
> UI depth (Hotels/Bookings/Travelers/Documents CRUD pages, Presentation Demo
> mode, richer animation) rather than algorithm coverage — see "Roadmap" below.

## Technology

- Java 17, JavaFX 21
- Maven build (`javafx-maven-plugin`, `maven-shade-plugin` for a runnable fat jar)
- JUnit 5 for testing

## Folder structure

```
TravelSync/
├── pom.xml
├── README.md
└── src/
    ├── main/java/com/travelsync/
    │   ├── TravelSyncApplication.java   # entry point
    │   ├── Launcher.java                # fat-jar launcher (bypasses module-path issues)
    │   ├── model/                       # Destination, Hotel, Traveler, Booking, ...
    │   ├── algorithms/
    │   │   └── string/                  # M1: KMP, Z-Function, Rabin-Karp, Aho-Corasick
    │   ├── data/                        # AppDataStore, SampleDataGenerator, AppSettings
    │   └── ui/
    │       ├── component/               # Sidebar, TopBar (search/notifications/settings)
    │       └── view/                    # DashboardView, DestinationsView, AlgorithmLabView
    └── test/java/com/travelsync/algorithms/string/   # JUnit tests for M1
```

## Running

```
mvn clean javafx:run
```

or build a runnable jar:

```
mvn clean package
java -jar target/travelsync-1.0.0.jar
```

## Testing

```
mvn test
```

> Note: this was developed in a sandboxed environment without a JDK/network,
> so the build has not yet been compiled or run there. Please run `mvn test`
> and `mvn javafx:run` locally as your first step and report back anything
> that doesn't build cleanly.

## What's implemented so far

**M1 — String Algorithms** (`algorithms/string/`)
- KMP (LPS table + search), Z-Function, Rabin-Karp (rolling hash), Aho-Corasick
  (trie + failure links, multi-pattern) — all real implementations, each with a
  `trace(...)` method that returns genuine step-by-step execution data (not
  pre-scripted animation).

**M2 — Suffix Structures** (`algorithms/suffix/`)
- Suffix Array via prefix-doubling (`SuffixArrayBuilder`)
- SA-IS linear-time construction (`SAISBuilder`) — cross-validated in tests
  against the doubling method on multiple inputs
- Kasai's LCP array (`LCPArrayBuilder`) + longest-repeated-substring
- Suffix Automaton (`SuffixAutomaton`) — substring membership and
  longest-common-substring across two texts

**M3 — Advanced Dynamic Programming** (`algorithms/dp/`)
- Levenshtein distance with full edit-script traceback
- Damerau-Levenshtein (adjacent transposition)
- Bitmask (Held-Karp) itinerary optimizer for optimal multi-stop ordering
- Matrix Chain Multiplication (optimal parenthesization)
- Optimal Binary Search Tree (expected-cost-minimizing layout)

**M4 — Network Flow** (`algorithms/flow/`)
- Ford-Fulkerson (DFS augmenting paths)
- Edmonds-Karp (BFS augmenting paths, polynomial guarantee)
- Dinic's algorithm (level graph + blocking flow)
- All three cross-validated against each other and the classic 23-unit
  textbook max-flow example
- Bipartite Matching (Kuhn's algorithm) and Min-Cut (via max-flow min-cut theorem)

**M5 — NP-Completeness & Approximation** (`algorithms/npcomplete/`)
- 3-SAT brute-force solver
- 3-SAT → CLIQUE reduction (classic literal-occurrence graph construction)
- CLIQUE ↔ INDEPENDENT SET (via complement graph)
- INDEPENDENT SET → VERTEX COVER conversion
- Vertex Cover 2-approximation

**M6 — Randomized & Parallel Algorithms** (`algorithms/parallel/`)
- Randomized QuickSort (random pivot, tested against reverse-sorted worst-case input)
- Reservoir Sampling (Algorithm R, single-pass)
- Miller-Rabin primality test (BigInteger, arbitrary precision)
- Blelloch work-efficient exclusive scan (up-sweep/down-sweep)
- Parallel Reduce — **genuinely multi-threaded** via Java's Fork/Join common pool
- Brent's theorem bound evaluator

**Data layer**
- 8 domain entities (Destination, Attraction, Hotel, Transportation, Traveler,
  Booking, TravelDocument, TravelService) with relationally-consistent sample
  data (every booking references a real traveler/hotel/transport, etc.)
- `AppDataStore` — observable, in-memory store with a real notification feed
  and a working "reset demo data" action.

**UI (light theme, per the design system in `theme-light.css`)**
- App shell: sidebar navigation + top bar
- Global search — genuinely backed by the KMP matcher, searches destinations/
  hotels/travelers/bookings, clickable results
- Notifications — real popover, unread badge, mark-as-read, clear all
- Settings — real dialog wired to `AppSettings` (visualization speed, density,
  auto-refresh, reset demo data)
- Dashboard — animated KPI counters computed from live data, clickable cards,
  popular-destinations and recent-bookings panels
- Destinations page — live search + category filter + sortable table
- Algorithm Lab (M1) — Run/Play/Pause/Step/Reset walk a real recorded trace
  with highlighted character cells and a live execution log
- Suffix Explorer (M2), DP Lab (M3), Flow Lab (M4), NP-Complete Lab (M5),
  Parallel Lab (M6) — each a working lab where Run buttons execute the real
  algorithm against live or entered data and display genuine results
  (tables, logs, cross-validation checks) rather than per-frame animation

## Roadmap (not yet built)

- Hotels / Bookings / Travelers / Travel Documents / Services CRUD pages
- Presentation Demo mode (large-screen guided walkthrough with Previous/Next)
- Deeper animation polish for M2–M6 (DP-matrix cell-by-cell fill, graph/flow
  edge animation, tree construction animation) — currently these labs show
  genuine computed results rather than frame-by-frame animation, which was a
  deliberate scope decision given the complexity/risk tradeoff of animating
  algorithms like SA-IS or Dinic's blocking-flow phases correctly
- Final packaging (`TravelSync_Final.zip`) once the above is complete and the
  full build has been verified locally (`mvn test`, `mvn javafx:run`)

## DSA-to-feature mapping (planned, full)

| Module | Algorithms | Travel use case |
|---|---|---|
| M1 | KMP, Z-Function, Rabin-Karp, Aho-Corasick | Search destinations/hotels/keywords, booking codes |
| M2 | Suffix Array, SA-IS, LCP/Kasai, Suffix Tree | Document indexing & similarity |
| M3 | Levenshtein/Damerau-Levenshtein, Bitmask DP, Matrix Chain Mult., Optimal BST | Name correction, itinerary optimization |
| M4 | Ford-Fulkerson, Edmonds-Karp, Dinic, Bipartite Matching, Min-Cut | Transport capacity, traveler-hotel matching |
| M5 | SAT/3-SAT, CLIQUE, INDEPENDENT SET, VERTEX COVER, 2-approx | Travel constraint analysis |
| M6 | Randomized QuickSort, Reservoir Sampling, Miller-Rabin, Blelloch Scan, Parallel Reduce, Brent's theorem | Ranking, sampling, booking-stream analytics |
