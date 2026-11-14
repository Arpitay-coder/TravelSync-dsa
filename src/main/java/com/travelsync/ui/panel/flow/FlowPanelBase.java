package com.travelsync.ui.panel.flow;

import com.travelsync.data.AppDataStore;
import com.travelsync.model.Destination;
import com.travelsync.ui.AlgorithmPanel;
import com.travelsync.ui.Ui;
import com.travelsync.util.ValidationException;
import com.travelsync.visualization.GraphVisualizer;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.function.Supplier;

/** Shared input controls and network construction of the max-flow and min-cut panels. */
abstract class FlowPanelBase extends AlgorithmPanel {

    static final String LIVE = "Live transport network";
    static final String TEXTBOOK = "Textbook network (max flow 23)";

    protected final GraphVisualizer graph = new GraphVisualizer(FlowNetwork.W, FlowNetwork.H);
    private final ComboBox<String> networkBox = new ComboBox<>();
    private final ComboBox<String> sourceBox = new ComboBox<>();
    private final ComboBox<String> sinkBox = new ComboBox<>();

    protected FlowPanelBase() {
        networkBox.getItems().addAll(LIVE, TEXTBOOK);
        networkBox.setValue(LIVE);
        List<Destination> destinations = AppDataStore.getInstance().getDestinations();
        for (Destination d : destinations) {
            sourceBox.getItems().add(d.getName());
            sinkBox.getItems().add(d.getName());
        }
        networkBox.valueProperty().addListener((obs, old, value) -> {
            boolean live = LIVE.equals(value);
            sourceBox.setDisable(!live);
            sinkBox.setDisable(!live);
        });
    }

    /** Input rows: network choice, source and sink, plus any panel-specific extra rows. */
    protected final Node flowInputs(Node... extraRows) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(Ui.label("Network", "kv-key"), 0, 0);
        grid.add(networkBox, 1, 0);
        grid.add(Ui.label("Source", "kv-key"), 0, 1);
        grid.add(sourceBox, 1, 1);
        grid.add(Ui.label("Sink", "kv-key"), 2, 1);
        grid.add(sinkBox, 3, 1);
        int row = 2;
        for (Node extra : extraRows) grid.add(extra, 0, row++, 4, 1);
        return grid;
    }

    protected final void loadFlowDemo() {
        networkBox.setValue(LIVE);
        int n = sourceBox.getItems().size();
        sourceBox.getSelectionModel().select(0);
        sinkBox.getSelectionModel().select(Math.max(0, n - 1));
    }

    /** @return a factory producing a fresh copy of the selected network (algorithms mutate the graph they run on) */
    protected final Supplier<FlowNetwork> networkFactory() throws ValidationException {
        if (TEXTBOOK.equals(networkBox.getValue())) return FlowNetwork::textbook;
        int source = sourceBox.getSelectionModel().getSelectedIndex();
        int sink = sinkBox.getSelectionModel().getSelectedIndex();
        if (source < 0 || sink < 0) throw new ValidationException("Please choose a source and a sink destination.");
        if (source == sink) throw new ValidationException("Source and sink must be different destinations.");
        AppDataStore store = AppDataStore.getInstance();
        List<Destination> destinations = List.copyOf(store.getDestinations());
        List<com.travelsync.model.Transportation> routes = List.copyOf(store.getTransportation());
        return () -> FlowNetwork.live(destinations, routes, source, sink);
    }
}
