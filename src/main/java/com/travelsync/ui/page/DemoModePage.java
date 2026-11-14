package com.travelsync.ui.page;

import com.travelsync.controller.NavigationController;
import com.travelsync.ui.AlgorithmCatalog;
import com.travelsync.ui.AlgorithmCatalog.Entry;
import com.travelsync.ui.Page;
import com.travelsync.ui.Ui;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/** Presentation Demo Mode: one-click walk through ten prepared algorithms in classroom order, each with the full pipeline. */
public final class DemoModePage implements Page {

    private static final String[] PIPELINE = {"REAL PROBLEM", "DSA TECHNIQUE", "INPUT", "ALGORITHM", "VISUALIZATION", "OUTPUT", "COMPLEXITY", "TRAVEL APPLICATION"};

    private final NavigationController navigation;
    private int current = 0;
    private VBox stepsBox;

    public DemoModePage(NavigationController navigation) {
        this.navigation = navigation;
    }

    @Override
    public String title() { return "Presentation Demo Mode"; }

    @Override
    public String subtitle() { return "A ready classroom walkthrough of ten algorithms across all six modules"; }

    @Override
    public Node content() {
        Label pipeline = Ui.label(String.join("  \u2192  ", PIPELINE), "subtle-text");
        pipeline.setWrapText(true);
        stepsBox = new VBox(10);
        rebuildSteps();
        Button restart = Ui.button("\u21BA Restart from step 1", "btn-secondary", "Reset the demo sequence");
        restart.setOnAction(ev -> { current = 0; rebuildSteps(); });
        VBox root = new VBox(14,
                Ui.label("Suggested presentation pipeline for every algorithm:", "section-title"), pipeline,
                Ui.label("Click \u201CRun this step\u201D to jump to each algorithm in order, with its demo data loaded and executed automatically.", "subtle-text"),
                restart, stepsBox);
        root.setPadding(new Insets(4, 24, 24, 24));
        return root;
    }

    private void rebuildSteps() {
        stepsBox.getChildren().clear();
        List<String> sequence = AlgorithmCatalog.demoSequence();
        for (int i = 0; i < sequence.size(); i++) {
            Entry e = AlgorithmCatalog.byName(sequence.get(i));
            stepsBox.getChildren().add(demoRow(i + 1, e));
        }
    }

    private HBox demoRow(int number, Entry e) {
        Label num = Ui.label(String.valueOf(number), "kpi-value");
        num.setMinWidth(40);
        VBox text = new VBox(2, Ui.label(e.name(), "algo-title"), Ui.label(e.module() + " \u00B7 " + e.route() + " \u00B7 " + e.application(), "subtle-text"));
        ((Label) text.getChildren().get(1)).setWrapText(true);
        HBox.setHgrow(text, Priority.ALWAYS);
        Button run = Ui.button("\u25B6 Run this step", "btn-primary", "Open " + e.route() + " and run " + e.name() + " with demo data");
        run.setOnAction(ev -> {
            navigation.goTo(e.route(), e.name(), true);
            current = number;
            rebuildSteps();
        });
        HBox row = new HBox(14, num, text, run);
        row.getStyleClass().add("card");
        row.setPadding(new Insets(10, 14, 10, 14));
        if (current == number) row.getStyleClass().add("demo-step-active");
        return row;
    }
}
