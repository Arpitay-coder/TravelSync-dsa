package com.travelsync.ui.page;

import com.travelsync.controller.NavigationController;
import com.travelsync.data.AppDataStore;
import com.travelsync.model.Booking;
import com.travelsync.model.Destination;
import com.travelsync.model.Hotel;
import com.travelsync.model.Transportation;
import com.travelsync.ui.Page;
import com.travelsync.ui.Ui;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Landing page: animated KPI cards, an animated travel-flow overview, five live charts and recent activity. */
public final class DashboardPage implements Page {

    private final NavigationController navigation;
    private final AppDataStore store = AppDataStore.getInstance();
    private final VBox root = new VBox(20);
    private final List<Label> flowChips = new ArrayList<>();
    private Timeline flowAnimation;

    public DashboardPage(NavigationController navigation) {
        this.navigation = navigation;
    }

    @Override
    public String title() { return "Dashboard"; }

    @Override
    public String subtitle() { return "Live overview of destinations, hotels, bookings, transport and travelers"; }

    @Override
    public Node content() {
        root.setPadding(new Insets(4, 24, 24, 24));
        rebuild();
        return root;
    }

    @Override
    public void onShow() {
        if (!root.getChildren().isEmpty()) rebuild();
    }

    private void rebuild() {
        root.getChildren().setAll(statGrid(), travelFlow(), chartGrid(), recentBookings());
    }

    // ------------------------------------------------------------------ KPI cards

    private GridPane statGrid() {
        long active = store.getBookings().stream()
                .filter(b -> b.getStatus() == Booking.Status.CONFIRMED || b.getStatus() == Booking.Status.PENDING).count();
        long upcoming = store.getBookings().stream()
                .filter(b -> b.getStatus() != Booking.Status.CANCELLED && b.getStartDate().isAfter(java.time.LocalDate.now())).count();
        long capacity = store.getTransportation().stream().mapToLong(t -> t.getCapacity() - t.getBooked()).sum();

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        Node[] cards = {
                statCard("\uD83D\uDCCD", store.getDestinations().size(), "Total Destinations", "badge-blue",
                        "All destinations in the catalogue", () -> navigation.goTo("Destinations", null, false)),
                statCard("\uD83C\uDFE8", store.getHotels().size(), "Available Hotels", "badge-teal",
                        "Hotels across every destination", () -> navigation.goTo("Destinations", null, false)),
                statCard("\uD83D\uDC64", store.getTravelers().size(), "Travelers", "badge-violet",
                        "Registered travelers", null),
                statCard("\uD83D\uDCC5", (int) active, "Active Bookings", "badge-amber",
                        "Confirmed or pending bookings", null),
                statCard("\u2708", (int) capacity, "Seats Available", "badge-blue",
                        "Free capacity across all transport routes", () -> navigation.goTo("Network Flow", "Dinic", true)),
                statCard("\uD83E\uDDF3", (int) upcoming, "Upcoming Trips", "badge-teal",
                        "Bookings starting after today", null)
        };
        for (int i = 0; i < cards.length; i++) grid.add(cards[i], i % 3, i / 3);
        columns(grid, 3);
        return grid;
    }

    private VBox statCard(String icon, int target, String label, String badgeClass, String tip, Runnable onClick) {
        Label iconLabel = Ui.label(icon, "stat-icon");
        Label badge = Ui.label(label, "badge", badgeClass);
        Label value = Ui.label("0", "kpi-value");
        HBox header = Ui.row(8, iconLabel, badge);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox card = Ui.card(header, value);
        card.getStyleClass().add("stat-card");
        card.setMaxWidth(Double.MAX_VALUE);
        Tooltip.install(card, new Tooltip(tip));
        if (onClick != null) {
            card.setCursor(javafx.scene.Cursor.HAND);
            card.setOnMouseClicked(e -> onClick.run());
        }
        Timeline counter = new Timeline();
        int steps = 24;
        for (int i = 0; i <= steps; i++) {
            int shown = Math.round(target * (i / (float) steps));
            counter.getKeyFrames().add(new KeyFrame(Duration.millis(i * 25.0), e -> value.setText(String.valueOf(shown))));
        }
        counter.play();
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

    // ------------------------------------------------------------------ travel flow

    /** Traveler -> Destination -> Hotel -> Transport -> Booking -> Experience, with an animated active step. */
    private VBox travelFlow() {
        String[] steps = {"Traveler", "Destination", "Attraction / Hotel", "Transportation", "Booking", "Travel Experience"};
        HBox flow = new HBox(8);
        flow.setAlignment(Pos.CENTER_LEFT);
        flowChips.clear();
        for (int i = 0; i < steps.length; i++) {
            Label chip = Ui.label(steps[i], "flow-chip");
            flowChips.add(chip);
            flow.getChildren().add(chip);
            if (i < steps.length - 1) flow.getChildren().add(Ui.label("\u2192", "subtle-text"));
        }
        if (flowAnimation != null) flowAnimation.stop();
        int[] active = {0};
        flowAnimation = new Timeline(new KeyFrame(Duration.millis(900), e -> {
            for (int i = 0; i < flowChips.size(); i++) {
                flowChips.get(i).getStyleClass().remove("flow-chip-active");
                if (i == active[0]) flowChips.get(i).getStyleClass().add("flow-chip-active");
            }
            active[0] = (active[0] + 1) % flowChips.size();
        }));
        flowAnimation.setCycleCount(Animation.INDEFINITE);
        flowAnimation.play();
        return Ui.card(Ui.label("Travel Flow", "section-title"), flow);
    }

    // ------------------------------------------------------------------ charts

    private GridPane chartGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.add(chartCard(popularDestinationsChart()), 0, 0);
        grid.add(chartCard(bookingStatusChart()), 1, 0);
        grid.add(chartCard(hotelUtilizationChart()), 0, 1);
        grid.add(chartCard(transportUsageChart()), 1, 1);
        grid.add(chartCard(bookingTrendChart()), 0, 2, 2, 1);
        columns(grid, 2);
        return grid;
    }

    private static VBox chartCard(Node chart) {
        VBox card = Ui.card(chart);
        card.setMinHeight(280);
        return card;
    }

    private VBox popularDestinationsChart() {
        VBox box = new VBox(10, Ui.label("Popular Destinations", "section-title"));
        List<Destination> top = store.getDestinations().stream()
                .sorted(Comparator.comparingDouble(Destination::getPopularityScore).reversed()).limit(6).toList();
        for (Destination d : top) {
            Label name = Ui.label(d.getName() + ", " + d.getCountry(), "");
            name.setPrefWidth(190);
            ProgressBar bar = new ProgressBar(d.getPopularityScore() / 100.0);
            bar.setPrefWidth(140);
            HBox row = Ui.row(10, name, bar, Ui.label(String.valueOf((int) d.getPopularityScore()), "subtle-text"));
            row.setAlignment(Pos.CENTER_LEFT);
            row.setCursor(javafx.scene.Cursor.HAND);
            row.setOnMouseClicked(e -> navigation.goTo("Destinations", null, false));
            box.getChildren().add(row);
        }
        return box;
    }

    private PieChart bookingStatusChart() {
        Map<Booking.Status, Integer> counts = new EnumMap<>(Booking.Status.class);
        for (Booking.Status s : Booking.Status.values()) counts.put(s, 0);
        for (Booking b : store.getBookings()) counts.merge(b.getStatus(), 1, Integer::sum);
        PieChart chart = new PieChart();
        counts.forEach((status, count) -> chart.getData().add(new PieChart.Data(status + " (" + count + ")", count)));
        chart.setTitle("Booking Status");
        chart.setLegendVisible(true);
        chart.setLabelsVisible(false);
        chart.setPrefHeight(230);
        return chart;
    }

    private BarChart<String, Number> hotelUtilizationChart() {
        Map<String, Number> data = new LinkedHashMap<>();
        for (Hotel h : store.getHotels()) {
            data.put(h.getId(), Math.round(h.getUtilization() * 1000) / 10.0);
        }
        return bar("Hotel Utilization (% of rooms occupied)", "Load %", data);
    }

    private BarChart<String, Number> transportUsageChart() {
        Map<String, Number> data = new LinkedHashMap<>();
        for (Transportation t : store.getTransportation()) {
            double percent = t.getCapacity() == 0 ? 0 : 100.0 * t.getBooked() / t.getCapacity();
            data.put(t.getId(), Math.round(percent * 10) / 10.0);
        }
        return bar("Transport Usage (% of seats booked)", "Load %", data);
    }

    private XYChart.Series<String, Number> series(String name, Map<String, Number> data) {
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName(name);
        data.forEach((k, v) -> s.getData().add(new XYChart.Data<>(k, v)));
        return s;
    }

    private BarChart<String, Number> bar(String title, String yLabel, Map<String, Number> data) {
        NumberAxis y = new NumberAxis();
        y.setLabel(yLabel);
        BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), y);
        chart.setTitle(title);
        chart.getData().add(series(title, data));
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefHeight(230);
        return chart;
    }

    private BarChart<String, Number> bookingTrendChart() {
        Map<String, Number> data = new LinkedHashMap<>();
        java.time.LocalDate start = java.time.LocalDate.now().minusDays(20);
        for (int i = 0; i < 30; i++) {
            java.time.LocalDate day = start.plusDays(i);
            long count = store.getBookings().stream()
                    .filter(b -> !b.getStartDate().isBefore(day) && b.getStartDate().isBefore(day.plusDays(1))).count();
            data.put(day.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM")), count);
        }
        return bar("Upcoming Trip Starts (next 30 days)", "Trips", data);
    }

    // ------------------------------------------------------------------ activity

    private VBox recentBookings() {
        VBox card = Ui.card(Ui.label("Recent Activity", "section-title"));
        List<Booking> recent = store.getBookings().stream()
                .sorted(Comparator.comparing(Booking::getCreatedOn).reversed()).limit(6).toList();
        for (Booking b : recent) {
            Destination d = store.findDestination(b.getDestinationId());
            Label desc = Ui.label(b.getId() + "  \u2192  " + (d != null ? d.getName() : b.getDestinationId()), "");
            desc.setPrefWidth(300);
            Label status = Ui.label(b.getStatus().toString(), "badge", switch (b.getStatus()) {
                case CONFIRMED -> "badge-success";
                case PENDING -> "badge-amber";
                case CANCELLED -> "badge-danger";
                case COMPLETED -> "badge-blue";
            });
            HBox row = Ui.row(12, desc, status, Ui.label("created " + b.getCreatedOn(), "subtle-text"));
            row.setAlignment(Pos.CENTER_LEFT);
            card.getChildren().add(row);
        }
        return card;
    }
}
