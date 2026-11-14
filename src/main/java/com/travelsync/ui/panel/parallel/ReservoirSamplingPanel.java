
package com.travelsync.ui.panel.parallel;

import com.travelsync.ui.AlgorithmPanel;
import com.travelsync.ui.Execution;
import com.travelsync.ui.Ui;
import com.travelsync.util.ValidationException;
import com.travelsync.visualization.ArrayVisualizer;
import com.travelsync.visualization.CellState;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Reservoir Sampling panel: keeps a uniform random sample of a continuous
 * booking stream, with replacements shown live.
 */
public final class ReservoirSamplingPanel extends AlgorithmPanel {

    private final Spinner<Integer> streamSize =
            new Spinner<>(5, 300, 40);

    private final Spinner<Integer> sampleSize =
            new Spinner<>(1, 10, 5);

    private final ArrayVisualizer visualizer = new ArrayVisualizer();

    public ReservoirSamplingPanel() {
        streamSize.setEditable(true);
        sampleSize.setEditable(true);

        streamSize.setPrefWidth(90);
        sampleSize.setPrefWidth(80);

        GridPane grid = new GridPane();
        grid.setHgap(10);

        grid.add(Ui.label("Stream length", "kv-key"), 0, 0);
        grid.add(streamSize, 1, 0);

        grid.add(Ui.label("Sample size (k)", "kv-key"), 2, 0);
        grid.add(sampleSize, 3, 0);

        assemble(
                "Reservoir Sampling",
                "Travel problem: keep a representative sample from a "
                        + "continuous, unbounded booking stream without "
                        + "storing all of it. Each item after the first k "
                        + "items replaces a uniformly random slot with "
                        + "probability k/i, keeping every item equally "
                        + "likely to appear in the final sample.",
                grid,
                visualizer
        );

        loadDemo();
    }

    @Override
    protected void loadDemo() {
        streamSize.getValueFactory().setValue(40);
        sampleSize.getValueFactory().setValue(5);
    }

    @Override
    protected Supplier<Execution> prepare()
            throws ValidationException {

        int n;
        int k;

        try {
            n = streamSize.getValue();
            k = sampleSize.getValue();
        } catch (NullPointerException ex) {
            throw new ValidationException(
                    "Please enter whole numbers for the stream "
                            + "and sample size."
            );
        }

        if (k > n) {
            throw new ValidationException(
                    "The sample size cannot exceed the stream length."
            );
        }

        return () -> execute(n, k);
    }

    private Execution execute(int n, int k) {
        Random random = new Random();

        int[] reservoir = new int[k];
        int[][] snapshots = new int[n][];
        int[] replacedSlot = new int[n];
        boolean[] accepted = new boolean[n];

        Arrays.fill(replacedSlot, -1);

        for (int i = 0; i < n; i++) {
            if (i < k) {
                reservoir[i] = i + 1;
                accepted[i] = true;
                replacedSlot[i] = i;
            } else {
                long j = (long) (random.nextDouble() * (i + 1));

                if (j < k) {
                    reservoir[(int) j] = i + 1;
                    accepted[i] = true;
                    replacedSlot[i] = (int) j;
                }
            }

            snapshots[i] = reservoir.clone();
        }

        String result =
                "Final sample (booking numbers): "
                        + Arrays.toString(snapshots[n - 1]);

        String complexity =
                "Time O(n) = O(" + n + ")"
                        + "   |   Space O(k) = O(" + k + ")";

        String info =
                "Keeping every item would require O(n) space. "
                        + "The reservoir stays fixed at k = " + k + ".";

        return new Execution(
                n,
                f -> draw(snapshots, replacedSlot, accepted, f),
                f -> narrate(k, f, replacedSlot, accepted),
                result,
                complexity,
                -1,
                n,
                info
        );
    }

    private void draw(
            int[][] snapshots,
            int[] replacedSlot,
            boolean[] accepted,
            int frame
    ) {
        int k = snapshots[frame].length;

        ArrayVisualizer.Cell[] row =
                new ArrayVisualizer.Cell[k];

        for (int i = 0; i < k; i++) {
            CellState state =
                    (accepted[frame] && replacedSlot[frame] == i)
                            ? CellState.ACTIVE
                            : CellState.NORMAL;

            row[i] = ArrayVisualizer.cell(
                    "#" + snapshots[frame][i],
                    state
            );
        }

        visualizer.render(
                List.of("Reservoir"),
                List.<ArrayVisualizer.Cell[]>of(row),
                accepted[frame] ? replacedSlot[frame] : -1
        );
    }

    private static String narrate(
            int k,
            int frame,
            int[] replacedSlot,
            boolean[] accepted
    ) {
        int i = frame + 1;

        if (i <= k) {
            return "Booking #" + i
                    + " fills empty slot " + (i - 1) + ".";
        }

        return accepted[frame]
                ? "Booking #" + i
                + " is kept with probability " + k + "/" + i
                + ": it replaces slot " + replacedSlot[frame] + "."
                : "Booking #" + i
                + " is discarded (probability "
                + (i - k) + "/" + i
                + "): the reservoir is unchanged.";
    }
}
