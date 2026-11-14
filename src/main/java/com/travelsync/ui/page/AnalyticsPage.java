package com.travelsync.ui.page;

import com.travelsync.algorithms.flow.DinicSolver;
import com.travelsync.algorithms.flow.FlowGraph;
import com.travelsync.data.AppDataStore;
import com.travelsync.model.*;
import com.travelsync.ui.Charts;
import com.travelsync.ui.Page;
import com.travelsync.ui.Tables;
import com.travelsync.ui.Ui;
import com.travelsync.util.AlgorithmStats;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Analytics: KPIs and charts computed from the live data, the transport flow network, and measured algorithm run statistics. */
public final class AnalyticsPage implements Page {

    private final AppDataStore store = AppDataStore.getInstance();
    private final VBox root = new VBox(16);

    @Override
    public String title() { return "Analytics"; }

    @Override
    public String subtitle() { return "Booking, hotel and network analytics \u00B7 algorithm statistics are measured, never invented"; }

    @Override
    public Node content() {
        root.setPadding(new Insets(4, 24, 24, 24));
        rebuild();
        return root;
    }

    @Override
    public void onShow() { rebuild(); }

    private void rebuild() {
        root.getChildren().setAll(kpis(), charts(), statsCard());
    }

    private static VBox kpi(String value, String caption, String tip) {
        VBox card = new VBox(4, Ui.label(value, "kpi-value"), Ui.label(caption, "kpi-label"));
        card.getStyleClass().addAll("card", "stat-card");
        card.setMaxWidth(Double.MAX_VALUE);
        Tooltip.install(card, new Tooltip(tip));
        return card;
    }

    private GridPane kpis() {
        List<Booking> all = store.getBookings();
        long done = all.stream().filter(b -> b.getStatus() == Booking.Status.COMPLETED).count();
        double avgDays = all.stream().mapToLong(b -> java.time.temporal.ChronoUnit.DAYS.between(b.getStartDate(), b.getEndDate())).average().orElse(0);
        double revenue = all.stream().mapToDouble(Booking::getTotalCost).sum();

        long sourceCapacity = store.getTransportation().stream()
                .filter(t -> t.getSourceDestinationId().equals(store.getDestinations().get(0).getId()))
                .mapToLong(Transportation::getCapacity).sum();
        long maxFlow = routeFlow();
        String utilisation = sourceCapacity == 0 ? "n/a" : String.format(Locale.ROOT, "%.0f%%", 100.0 * maxFlow / sourceCapacity);

        GridPane g = new GridPane();
        g.setHgap(14);
        g.setVgap(14);
        Node[] cards = {
                kpi(String.valueOf(all.size()), "Booking volume", String.format(Locale.ROOT, "$%,.0f total booking value", revenue)),
                kpi(all.isEmpty() ? "\u2014" : String.format(Locale.ROOT, "%.0f%% / %.0f%%", 100.0 * done / all.size(), 100.0 * (all.size() - done) / all.size()),
                        "Completed vs other", "Completed bookings vs. pending/confirmed/cancelled"),
                kpi(String.format(Locale.ROOT, "%.1f days", avgDays), "Average trip length", "Average number of nights across all bookings"),
                kpi(utilisation, "Transport network utilisation",
                        "Dinic max flow from " + store.getDestinations().get(0).getName() + " \u00F7 capacity of its outgoing routes. Max flow = " + maxFlow)};
        for (int i = 0; i < cards.length; i++) g.add(cards[i], i, 0);
        columns(g, 4);
        return g;
    }

    private long routeFlow() {
        List<Destination> destinations = store.getDestinations();
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < destinations.size(); i++) index.put(destinations.get(i).getId(), i);
        FlowGraph g = new FlowGraph(destinations.size());
        for (Transportation t : store.getTransportation()) {
            Integer from = index.get(t.getSourceDestinationId());
            Integer to = index.get(t.getTargetDestinationId());
            if (from != null && to != null) g.addEdge(from, to, t.getCapacity());
        }
        return DinicSolver.solve(g, 0, destinations.size() - 1);
    }

    private GridPane charts() {
        Map<String, Number> byDestination = new LinkedHashMap<>();
        for (Destination d : store.getDestinations()) {
            long c = store.getBookings().stream().filter(b -> b.getDestinationId().equals(d.getId())).count();
            if (c > 0) byDestination.put(d.getName(), c);
        }

        Map<String, Number> completed = new LinkedHashMap<>();
        Map<String, Number> other = new LinkedHashMap<>();
        Map<String, List<Destination>> byCategory = new LinkedHashMap<>();
        for (Destination d : store.getDestinations()) byCategory.computeIfAbsent(d.getCategory(), k -> new java.util.ArrayList<>()).add(d);
        for (String category : byCategory.keySet()) {
            long c = store.getBookings().stream().filter(b -> {
                Destination d = store.findDestination(b.getDestinationId());
                return d != null && d.getCategory().equals(category) && b.getStatus() == Booking.Status.COMPLETED;
            }).count();
            long o = store.getBookings().stream().filter(b -> {
                Destination d = store.findDestination(b.getDestinationId());
                return d != null && d.getCategory().equals(category) && b.getStatus() != Booking.Status.COMPLETED;
            }).count();
            completed.put(category, c);
            other.put(category, o);
        }

        Map<String, Number> loyalty = new LinkedHashMap<>();
        for (Traveler t : store.getTravelers()) loyalty.put(t.getId(), t.getLoyaltyPoints());

        Map<String, Number> occupied = new LinkedHashMap<>();
        Map<String, Number> capacity = new LinkedHashMap<>();
        for (Hotel h : store.getHotels()) {
            occupied.put(h.getId(), h.getTotalRooms() - h.getAvailableRooms());
            capacity.put(h.getId(), h.getTotalRooms());
        }

        Map<String, Number> routeCapacity = new LinkedHashMap<>();
        Map<String, Number> routeBooked = new LinkedHashMap<>();
        for (Transportation t : store.getTransportation()) {
            routeCapacity.put(t.getId(), t.getCapacity());
            routeBooked.put(t.getId(), t.getBooked());
        }

        Map<String, Map<String, Number>> byStatus = new LinkedHashMap<>();
        byStatus.put("Completed", completed);
        byStatus.put("Pending / active", other);
        Map<String, Map<String, Number>> hotelSeries = new LinkedHashMap<>();
        hotelSeries.put("Occupied rooms", occupied);
        hotelSeries.put("Total capacity", capacity);
        Map<String, Map<String, Number>> routeSeries = new LinkedHashMap<>();
        routeSeries.put("Route capacity", routeCapacity);
        routeSeries.put("Seats booked", routeBooked);

        GridPane g = new GridPane();
        g.setHgap(14);
        g.setVgap(14);
        Node[] cards = {
                chartCard(Charts.bar("Bookings by destination", "Bookings", "Bookings", byDestination, true)),
                chartCard(Charts.grouped("Completed vs pending by destination category", "Bookings", byStatus, true, true)),
                chartCard(Charts.bar("Traveler loyalty points", "Points", "Points", loyalty, true)),
                chartCard(Charts.grouped("Hotel occupancy vs capacity", "Rooms", hotelSeries, true, true)),
                chartCard(Charts.grouped("Transport route capacity vs bookings (Dinic-analysed network)", "Seats", routeSeries, true, true))};
        for (int i = 0; i < cards.length; i++) g.add(cards[i], i % 2, i / 2);
        columns(g, 2);
        return g;
    }

    private static VBox chartCard(Node chart) {
        VBox card = Ui.card(chart);
        card.setMinHeight(280);
        return card;
    }

    private static void columns(GridPane grid, int count) {
        for (int i = 0; i < count; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / count);
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }
    }

    private VBox statsCard() {
        TableView<AlgorithmStats.Row> table = new TableView<>();
        table.getColumns().add(Tables.column("Algorithm", 300, AlgorithmStats.Row::algorithm));
        table.getColumns().add(Tables.column("Runs", 60, r -> String.valueOf(r.runs())));
        table.getColumns().add(Tables.column("Last run (ms)", 110, r -> String.format(Locale.ROOT, "%.3f", r.lastMillis())));
        table.getColumns().add(Tables.column("Average (ms)", 110, r -> String.format(Locale.ROOT, "%.3f", r.averageMillis())));
        table.getColumns().add(Tables.column("Last comparisons", 150, r -> r.lastComparisons() < 0 ? "n/a" : String.format(Locale.ROOT, "%,d", r.lastComparisons())));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getItems().setAll(AlgorithmStats.snapshot());
        table.setPrefHeight(280);
        table.setPlaceholder(new Label("No algorithm has been run yet \u2013 use Demo Mode or any algorithm page, then come back."));
        return Ui.card(Ui.label("ALGORITHM EXECUTION STATISTICS (measured in this session)", "section-title"), table);
    }
}
