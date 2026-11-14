package com.travelsync.ui.view;

import com.travelsync.algorithms.string.KMPMatcher;
import com.travelsync.data.AppDataStore;
import com.travelsync.model.Destination;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Destinations catalogue: live search (backed by the real KMP matcher, not a
 * placeholder .contains()), a category filter, and a fully sortable table --
 * every column header actually sorts, every filter actually filters.
 */
public class DestinationsView extends BorderPane {

    public DestinationsView() {
        AppDataStore store = AppDataStore.getInstance();
        ObservableList<Destination> all = store.getDestinations();

        VBox root = new VBox(16);
        root.setPadding(new Insets(4));
        root.getStyleClass().add("content-area");

        Label title = new Label("Destinations");
        title.getStyleClass().add("section-title");

        TextField searchField = new TextField();
        searchField.setPromptText("Search destinations by name (KMP-powered)...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(320);

        ComboBox<String> categoryFilter = new ComboBox<>();
        categoryFilter.getItems().add("All Categories");
        all.stream().map(Destination::getCategory).distinct().sorted().forEach(categoryFilter.getItems()::add);
        categoryFilter.setValue("All Categories");

        HBox controls = new HBox(12, searchField, categoryFilter);
        controls.setAlignment(Pos.CENTER_LEFT);

        FilteredList<Destination> filtered = new FilteredList<>(all, d -> true);
        Runnable applyFilter = () -> filtered.setPredicate(d -> {
            String query = searchField.getText();
            boolean matchesSearch = query == null || query.isBlank()
                    || !KMPMatcher.search(d.getName().toLowerCase(), query.toLowerCase()).isEmpty();
            String cat = categoryFilter.getValue();
            boolean matchesCategory = cat == null || cat.equals("All Categories") || cat.equals(d.getCategory());
            return matchesSearch && matchesCategory;
        });
        searchField.textProperty().addListener((obs, o, n) -> applyFilter.run());
        categoryFilter.valueProperty().addListener((obs, o, n) -> applyFilter.run());

        TableView<Destination> table = new TableView<>();
        TableColumn<Destination, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Destination, String> countryCol = new TableColumn<>("Country");
        countryCol.setCellValueFactory(new PropertyValueFactory<>("country"));
        TableColumn<Destination, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        TableColumn<Destination, Double> popularityCol = new TableColumn<>("Popularity");
        popularityCol.setCellValueFactory(new PropertyValueFactory<>("popularityScore"));
        TableColumn<Destination, Double> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("averageRating"));
        table.getColumns().addAll(nameCol, countryCol, categoryCol, popularityCol, ratingCol);

        SortedList<Destination> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        Label emptyLabel = new Label("No destinations match your search.");
        emptyLabel.getStyleClass().add("subtle-text");
        table.setPlaceholder(emptyLabel);

        Label resultCount = new Label();
        resultCount.getStyleClass().add("subtle-text");
        resultCount.textProperty().bind(Bindings.size(filtered).asString("%d destination(s) found"));

        root.getChildren().addAll(title, controls, resultCount, table);
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);
        setCenter(root);
    }
}
