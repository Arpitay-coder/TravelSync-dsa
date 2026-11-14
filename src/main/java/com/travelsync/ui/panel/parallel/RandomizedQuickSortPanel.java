
package com.travelsync.ui.panel.parallel;

import com.travelsync.algorithms.parallel.RandomizedQuickSort;
import com.travelsync.data.AppDataStore;
import com.travelsync.ui.AlgorithmPanel;
import com.travelsync.ui.Execution;
import com.travelsync.ui.Ui;
import com.travelsync.util.ValidationException;
import com.travelsync.visualization.ArrayVisualizer;
import com.travelsync.visualization.CellState;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Randomized QuickSort panel: ranks destinations by popularity,
 * showing every pivot choice and partition.
 */
public final class RandomizedQuickSortPanel extends AlgorithmPanel {

    private static final int MAX_VALUES = 14;

    private final TextField valuesField = new TextField();
    private final CheckBox descending =
            new CheckBox("Best (highest) first");
    private final ArrayVisualizer visualizer = new ArrayVisualizer();

    public RandomizedQuickSortPanel() {
        descending.setSelected(true);

        Button useData = Ui.button(
                "Use destination popularity scores",
                "btn-secondary",
                "Fill the values with live popularity scores"
        );

        useData.setOnAction(e ->
                valuesField.setText(
                        AppDataStore.getInstance()
                                .getDestinations()
                                .stream()
                                .map(d -> String.valueOf(
                                        (int) d.getPopularityScore()
                                ))
                                .collect(Collectors.joining(", "))
                )
        );

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        grid.add(Ui.label("Values", "kv-key"), 0, 0);
        grid.add(valuesField, 1, 0);
        grid.add(useData, 2, 0);
        grid.add(descending, 1, 1);

        GridPane.setHgrow(valuesField, Priority.ALWAYS);

        assemble(
                "Randomized QuickSort",
                "Travel problem: rank destinations by a preference score. "
                        + "A uniformly random pivot is chosen at every step, "
                        + "so even sorted or reverse-sorted input typically "
                        + "runs in expected O(n log n), unlike a fixed-pivot "
                        + "QuickSort, which can degrade to O(n^2).",
                grid,
                visualizer
        );

        loadDemo();
    }

    @Override
    protected void loadDemo() {
        valuesField.setText("70, 95, 60, 88, 75, 92, 81");
        descending.setSelected(true);
    }

    @Override
    protected Supplier<Execution> prepare()
            throws ValidationException {

        String raw = valuesField.getText();

        if (raw == null || raw.isBlank()) {
            throw new ValidationException(
                    "Please enter some values."
            );
        }

        int[] values;

        try {
            values = List.of(raw.split(","))
                    .stream()
                    .mapToInt(s -> Integer.parseInt(s.trim()))
                    .toArray();
        } catch (NumberFormatException ex) {
            throw new ValidationException(
                    "Values must be whole numbers separated by commas."
            );
        }

        if (values.length < 2 || values.length > MAX_VALUES) {
            throw new ValidationException(
                    "Please enter between 2 and "
                            + MAX_VALUES + " values."
            );
        }

        boolean desc = descending.isSelected();

        return () -> execute(values, desc);
    }

    private Execution execute(int[] values, boolean desc) {

        List<RandomizedQuickSort.Frame> frames =
                RandomizedQuickSort.trace(
                        values,
                        desc,
                        new Random()
                );

        RandomizedQuickSort.Frame last =
                frames.get(frames.size() - 1);

        int n = values.length;

        String result =
                "Ranked: " + java.util.Arrays.toString(last.data());

        String complexity =
                "Expected time O(n log n) | Worst case O(n^2)";

        String info = last.comparisons()
                + " comparisons used, out of an expected approximately "
                + Math.round(
                1.39 * n * (Math.log(n) / Math.log(2))
        )
                + " for n = " + n + ".";

        return new Execution(
                frames.size(),
                f -> draw(frames, f),
                f -> frames.get(f).label(),
                result,
                complexity,
                last.comparisons(),
                frames.size(),
                info
        );
    }

    private void draw(
            List<RandomizedQuickSort.Frame> frames,
            int frame
    ) {
        RandomizedQuickSort.Frame f = frames.get(frame);

        int n = f.data().length;

        ArrayVisualizer.Cell[] row =
                new ArrayVisualizer.Cell[n];

        for (int i = 0; i < n; i++) {

            CellState state = CellState.NORMAL;

            if (f.fixed()[i]) {
                state = CellState.DONE;
            } else if (i < f.lo() || i > f.hi()) {
                state = CellState.MUTED;
            } else if (i == f.pivotIndex()) {
                state = CellState.HIGHLIGHT;
            } else if (i == f.scanIndex()) {
                state = CellState.ACTIVE;
            } else if (i < f.boundary()) {
                state = CellState.MATCH;
            }

            row[i] = ArrayVisualizer.cell(
                    String.valueOf(f.data()[i]),
                    state
            );
        }

        visualizer.render(
                List.of("Values"),
                List.<ArrayVisualizer.Cell[]>of(row),
                Math.max(0, f.scanIndex())
        );
    }
}
