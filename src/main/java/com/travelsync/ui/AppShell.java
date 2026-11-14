package com.travelsync.ui;

import com.travelsync.controller.NavigationController;
import com.travelsync.controller.NavigationController.Route;
import com.travelsync.data.AppDataStore;
import com.travelsync.data.AppNotification;
import com.travelsync.data.AppSettings;
import com.travelsync.algorithms.string.KMPMatcher;
import com.travelsync.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Popup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Main window layout: brand + sidebar navigation on the left, top bar and page host on the right. */
public final class AppShell extends BorderPane {

    private final NavigationController navigation;
    private final StackPane pageHost = new StackPane();
    private final Label pageTitle = Ui.label("", "page-title-lg");
    private final Label pageSubtitle = Ui.label("", "subtle-text");
    private final Map<Route, Button> buttons = new HashMap<>();
    private final Map<Route, ScrollPane> scrollers = new HashMap<>();
    private final Popup searchResultsPopup = new Popup();
    private final Popup notificationsPopup = new Popup();
    private final Label notifBadge = new Label();
    private final AppDataStore store = AppDataStore.getInstance();

    public AppShell(NavigationController navigation) {
        this.navigation = navigation;
        getStyleClass().add("app-shell");
        setLeft(buildSidebar());
        setCenter(buildMain());
        navigation.setNavigator(this::navigate);
        navigation.currentProperty().addListener((obs, old, route) -> markSelected(route));
        navigate(navigation.routes().get(0));
    }

    private VBox buildSidebar() {
        Label brand = Ui.label("TravelSync", "sidebar-brand");
        Label tagline = Ui.label("Intelligent Travel\nPlanning Platform", "sidebar-subtitle");
        VBox nav = new VBox(4);
        for (Route route : navigation.routes()) {
            Button button = new Button(route.icon() + "   " + route.label());
            button.getStyleClass().add("nav-item");
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(e -> navigate(route));
            buttons.put(route, button);
            nav.getChildren().add(button);
        }
        ScrollPane scroll = new ScrollPane(nav);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        VBox sidebar = new VBox(6, new VBox(2, brand, tagline), scroll);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20, 12, 12, 12));
        sidebar.setPrefWidth(248);
        sidebar.setMinWidth(248);
        return sidebar;
    }

    private BorderPane buildMain() {
        VBox titles = new VBox(2, pageTitle, pageSubtitle);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField search = new TextField();
        search.setPromptText("Search destinations, hotels, travelers, bookings...");
        search.getStyleClass().add("search-field");
        search.setPrefWidth(360);
        search.textProperty().addListener((obs, o, n) -> showSearchResults(search, n));

        Button notifBtn = new Button("\uD83D\uDD14");
        notifBtn.getStyleClass().add("icon-button");
        notifBtn.setOnAction(e -> toggleNotifications(notifBtn));
        notifBadge.getStyleClass().addAll("badge", "badge-danger");
        notifBadge.setVisible(false);
        StackPane badgeWrap = new StackPane(notifBtn, notifBadge);
        StackPane.setAlignment(notifBadge, Pos.TOP_RIGHT);
        store.getNotifications().addListener((javafx.collections.ListChangeListener<AppNotification>) c -> refreshBadge());
        refreshBadge();

        Button settingsBtn = new Button("\u2699");
        settingsBtn.getStyleClass().add("icon-button");
        settingsBtn.setOnAction(e -> openSettingsDialog());

        Label statusChip = Ui.label("\u25CF Data synced", "chip-ok");

        Button themeToggle = new Button();
        themeToggle.getStyleClass().addAll("btn-secondary", "theme-toggle");
        Runnable refreshThemeLabel = () -> themeToggle.setText(
                com.travelsync.util.ThemeManager.isLight() ? "\uD83C\uDF19 Dark mode" : "\u2600 Light mode");
        themeToggle.setTooltip(new Tooltip("Switch between the light and the dark theme"));
        themeToggle.setOnAction(e -> {
            com.travelsync.util.ThemeManager.toggle();
            refreshThemeLabel.run();
        });
        com.travelsync.util.ThemeManager.addListener(refreshThemeLabel);
        refreshThemeLabel.run();

        HBox topBar = new HBox(14, titles, spacer, statusChip, search, themeToggle, badgeWrap, settingsBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("topbar");
        topBar.setPadding(new Insets(14, 24, 14, 24));
        BorderPane main = new BorderPane();
        main.setTop(topBar);
        main.setCenter(pageHost);
        return main;
    }

    private void navigate(Route route) {
        Page page = navigation.open(route);
        pageTitle.setText(page.title());
        pageSubtitle.setText(page.subtitle());
        ScrollPane scroll = scrollers.computeIfAbsent(route, r -> {
            ScrollPane created = new ScrollPane(page.content());
            created.setFitToWidth(true);
            created.getStyleClass().add("scroll-pane");
            return created;
        });
        pageHost.getChildren().setAll(scroll);
    }

    private void markSelected(Route selected) {
        buttons.forEach((route, button) -> {
            button.getStyleClass().remove("nav-item-selected");
            if (route.equals(selected)) button.getStyleClass().add("nav-item-selected");
        });
    }

    // ---- Global search (unchanged behaviour from the previous top bar) ----

    private void refreshBadge() {
        long unread = store.getNotifications().stream().filter(n -> !n.isRead()).count();
        notifBadge.setText(String.valueOf(unread));
        notifBadge.setVisible(unread > 0);
    }

    private void showSearchResults(TextField anchor, String query) {
        searchResultsPopup.hide();
        searchResultsPopup.getContent().clear();
        if (query == null || query.isBlank()) return;

        VBox panel = new VBox(2);
        panel.getStyleClass().add("card");
        panel.setPrefWidth(420);
        List<javafx.scene.Node> results = new ArrayList<>();
        String q = query.toLowerCase();

        for (Destination d : store.getDestinations()) {
            if (!KMPMatcher.search(d.getName().toLowerCase(), q).isEmpty()) {
                results.add(resultRow("\uD83D\uDCCD Destination", d.getName() + ", " + d.getCountry(),
                        () -> { navigation.goTo("Destinations", null, false); searchResultsPopup.hide(); }));
            }
        }
        for (Hotel h : store.getHotels()) {
            if (!KMPMatcher.search(h.getName().toLowerCase(), q).isEmpty()) {
                results.add(resultRow("\uD83C\uDFE8 Hotel", h.getName(), () -> {
                    showInfo("Hotel", h.getName() + "\n" + h.getStarRating() + "-star, $" + h.getPricePerNight()
                            + "/night, " + h.getAvailableRooms() + " rooms available");
                    searchResultsPopup.hide();
                }));
            }
        }
        for (Traveler t : store.getTravelers()) {
            if (!KMPMatcher.search(t.getFullName().toLowerCase(), q).isEmpty()) {
                results.add(resultRow("\uD83D\uDC64 Traveler", t.getFullName(), () -> {
                    showInfo("Traveler", t.getFullName() + "\n" + t.getEmail() + "\nLoyalty points: " + t.getLoyaltyPoints());
                    searchResultsPopup.hide();
                }));
            }
        }
        for (Booking b : store.getBookings()) {
            if (!KMPMatcher.search(b.getId().toLowerCase(), q).isEmpty()) {
                results.add(resultRow("\uD83D\uDCC5 Booking", b.getId() + " (" + b.getStatus() + ")", () -> {
                    showInfo("Booking", b.getId() + "\nStatus: " + b.getStatus() + "\nDates: " + b.getStartDate() + " to " + b.getEndDate());
                    searchResultsPopup.hide();
                }));
            }
        }

        if (results.isEmpty()) {
            Label none = new Label("No results for \"" + query + "\"");
            none.getStyleClass().add("subtle-text");
            none.setPadding(new Insets(8));
            panel.getChildren().add(none);
        } else {
            panel.getChildren().addAll(results.subList(0, Math.min(8, results.size())));
        }

        panel.getStylesheets().add(currentStylesheet());
        searchResultsPopup.getContent().add(panel);
        var bounds = anchor.localToScreen(anchor.getBoundsInLocal());
        if (bounds != null) searchResultsPopup.show(anchor, bounds.getMinX(), bounds.getMaxY() + 4);
    }

    private HBox resultRow(String typeLabel, String text, Runnable onClick) {
        HBox row = new HBox(8);
        row.setPadding(new Insets(6));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setCursor(javafx.scene.Cursor.HAND);
        Label type = new Label(typeLabel);
        type.getStyleClass().add("subtle-text");
        type.setPrefWidth(100);
        row.getChildren().addAll(type, new Label(text));
        row.setOnMouseClicked(e -> onClick.run());
        row.getStyleClass().add("search-result-row");
        return row;
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);
        com.travelsync.util.ThemeManager.style(alert.getDialogPane());
        alert.showAndWait();
    }

    private void toggleNotifications(Button anchor) {
        if (notificationsPopup.isShowing()) { notificationsPopup.hide(); return; }
        notificationsPopup.getContent().clear();
        VBox panel = new VBox(6);
        panel.getStyleClass().add("card");
        panel.setPrefWidth(340);

        Label title = new Label("Notifications");
        title.getStyleClass().add("section-title");
        Button clearBtn = new Button("Clear all");
        clearBtn.getStyleClass().add("btn-secondary");
        clearBtn.setOnAction(e -> { store.getNotifications().clear(); notificationsPopup.hide(); refreshBadge(); });
        HBox header = new HBox(10, title, clearBtn);
        HBox.setHgrow(title, Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);
        panel.getChildren().add(header);

        if (store.getNotifications().isEmpty()) {
            Label empty = new Label("You're all caught up.");
            empty.getStyleClass().add("subtle-text");
            panel.getChildren().add(empty);
        }
        for (AppNotification n : store.getNotifications()) {
            VBox row = new VBox(2);
            row.setPadding(new Insets(8));
            row.getStyleClass().add(n.isRead() ? "nav-item" : "badge-blue");
            row.setStyle("-fx-background-radius: 10; -fx-cursor: hand;");
            Label t = new Label((n.isRead() ? "" : "\u25CF ") + n.getTitle());
            t.setStyle("-fx-font-weight: bold;");
            Label m = new Label(n.getMessage());
            m.setWrapText(true);
            m.getStyleClass().add("subtle-text");
            Label time = new Label(n.getFormattedTime());
            time.getStyleClass().add("subtle-text");
            row.getChildren().addAll(t, m, time);
            row.setOnMouseClicked(e -> { n.setRead(true); refreshBadge(); notificationsPopup.hide(); toggleNotifications(anchor); });
            panel.getChildren().add(row);
        }
        panel.getStylesheets().add(currentStylesheet());
        notificationsPopup.getContent().add(panel);
        var bounds = anchor.localToScreen(anchor.getBoundsInLocal());
        if (bounds != null) notificationsPopup.show(anchor, bounds.getMinX() - 280, bounds.getMaxY() + 4);
    }

    private static String currentStylesheet() {
        return com.travelsync.util.ThemeManager.isLight() ? com.travelsync.util.Theme.LIGHT_STYLESHEET : com.travelsync.util.Theme.DARK_STYLESHEET;
    }

    private void openSettingsDialog() {
        AppSettings settings = AppSettings.getInstance();
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Settings");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        com.travelsync.util.ThemeManager.style(dialog.getDialogPane());

        VBox content = new VBox(14);
        content.setPadding(new Insets(16));
        Label speedLabel = new Label("Default visualization speed");
        ComboBox<String> speedBox = new ComboBox<>();
        speedBox.getItems().addAll("Slow", "Normal", "Fast");
        speedBox.setValue(settings.getStepDurationMillis() >= 1000 ? "Slow" : settings.getStepDurationMillis() <= 300 ? "Fast" : "Normal");
        speedBox.valueProperty().addListener((obs, o, n) -> settings.setStepDurationMillis(
                switch (n) { case "Slow" -> 1200; case "Fast" -> 220; default -> 600; }));

        Label densityLabel = new Label("UI density");
        ComboBox<AppSettings.Density> densityBox = new ComboBox<>();
        densityBox.getItems().addAll(AppSettings.Density.values());
        densityBox.setValue(settings.getDensity());
        densityBox.valueProperty().addListener((obs, o, n) -> settings.setDensity(n));

        CheckBox autoRefreshBox = new CheckBox("Auto-refresh dashboard");
        autoRefreshBox.setSelected(settings.isAutoRefresh());
        autoRefreshBox.selectedProperty().addListener((obs, o, n) -> settings.setAutoRefresh(n));

        Button resetDataBtn = new Button("Reset demo data");
        resetDataBtn.getStyleClass().add("btn-secondary");
        resetDataBtn.setOnAction(e -> { store.resetDemoData(); refreshBadge(); });

        content.getChildren().addAll(speedLabel, speedBox, densityLabel, densityBox, autoRefreshBox, new Separator(), resetDataBtn);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }
}
