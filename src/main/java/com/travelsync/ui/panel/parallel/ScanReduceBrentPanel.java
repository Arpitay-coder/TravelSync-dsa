package com.travelsync.ui.panel.parallel;

import com.travelsync.algorithms.parallel.BlellochScan;
import com.travelsync.algorithms.parallel.BrentsTheoremEvaluator;
import com.travelsync.algorithms.parallel.ParallelReducer;
import com.travelsync.ui.AlgorithmPanel;
import com.travelsync.ui.Execution;
import com.travelsync.ui.Ui;
import com.travelsync.util.ValidationException;
import com.travelsync.visualization.ArrayVisualizer;
import com.travelsync.visualization.CellState;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/** Blelloch Scan + Parallel Reduce + Brent's theorem panel:
 * cumulative booking totals and large-scale demand aggregation.
 */
public final class ScanReduceBrentPanel extends AlgorithmPanel {

    private final TextField dataField = new TextField();
    private final ArrayVisualizer visualizer = new ArrayVisualizer();

    public ScanReduceBrentPanel() {
        dataField.setPromptText("Daily bookings, comma separated");

        GridPane grid = new GridPane();
        grid.setHgap(10);

        grid.add(Ui.label("Daily bookings", "kv-key"), 0, 0);
        grid.add(dataField, 1, 0);

        GridPane.setHgrow(dataField, Priority.ALWAYS);

        assemble(
                "Blelloch Scan, Parallel Reduce & Brent's theorem",
                "Travel problem: cumulative booking totals and large-scale demand aggregation. "
                        + "The Blelloch scan computes all prefix sums in an up-sweep (partial sums) "
                        + "and down-sweep (distribution) over a balanced tree – O(n) work, O(log n) depth, "
                        + "which is what makes it parallelizable. Parallel Reduce then sums two million "
                        + "values on Java's Fork/Join pool, and Brent's theorem bounds how much that "
                        + "parallel work can help.",
                grid,
                visualizer
        );

        loadDemo();
    }

    @Override
    protected void loadDemo() {
        dataField.setText("12, 18, 9, 25, 14, 30, 7, 21, 16, 11");
    }

    @Override
    protected Supplier<Execution> prepare() throws ValidationException {
        String raw = dataField.getText();

        if (raw == null || raw.isBlank()) {
            throw new ValidationException(
                    "Please enter some daily booking counts."
            );
        }

        long[] data;

        try {
            data = List.of(raw.split(","))
                    .stream()
                    .mapToLong(s -> Long.parseLong(s.trim()))
                    .toArray();
        } catch (NumberFormatException ex) {
            throw new ValidationException(
                    "Values must be whole numbers separated by commas."
            );
        }

        if (data.length < 2 || data.length > 16) {
            throw new ValidationException(
                    "Please enter between 2 and 16 values."
            );
        }

        return () -> execute(data);
    }

    private Execution execute(long[] data) {

        BlellochScan.Trace scan = BlellochScan.trace(data);

        long[] big = new long[2_000_000];

        Random r = new Random(1);
        long expected = 0;

        for (int i = 0; i < big.length; i++) {
            big[i] = r.nextInt(100);
            expected += big[i];
        }

        int processors =
                Runtime.getRuntime().availableProcessors();

        long start = System.nanoTime();

        long sum =
                ParallelReducer.reduceSum(
                        big,
                        processors
                );

        long parallelMs =
                (System.nanoTime() - start) / 1_000_000;

        start = System.nanoTime();

        long sumSeq =
                ParallelReducer.reduceSum(
                        big,
                        1
                );

        long seqMs =
                (System.nanoTime() - start) / 1_000_000;

        List<long[]> levels =
                ParallelReducer.reductionLevels(
                        java.util.Arrays.copyOf(
                                data,
                                data.length
                        )
                );

        long depth =
                (long) Math.ceil(
                        Math.log(big.length) / Math.log(2)
                );

        var bound =
                BrentsTheoremEvaluator.computeBound(
                        big.length,
                        depth,
                        processors
                );

        int scanFrames =
                scan.steps().size();

        int reduceFrames =
                levels.size();

        int frames =
                scanFrames + reduceFrames + 1;

        String result =
                "Cumulative total before each day: "
                        + java.util.Arrays.toString(scan.result())
                        + "\nSum of "
                        + big.length
                        + " values: "
                        + sum
                        + (sum == sumSeq && sum == expected
                        ? " (matches sequential sum)"
                        : " (MISMATCH)");

        String complexity =
                "Scan: O(n) work, O(log n) depth   |   "
                        + "Reduce: O(n) work, O(log n) depth   |   "
                        + "Brent: T_p ≤ W/p + D";

        String info =
                String.format(
                        "Measured: %d processor(s), parallel %dms vs. "
                                + "single-thread %dms (%.1fx). "
                                + "Brent bound with these work/depth figures: "
                                + "%.0f–%.0f (arbitrary time units, illustrative).",
                        processors,
                        parallelMs,
                        seqMs,
                        seqMs / (double) Math.max(1, parallelMs),
                        bound.lowerBound(),
                        bound.upperBound()
                );

        return new Execution(
                frames,
                f -> draw(
                        data,
                        scan,
                        levels,
                        scanFrames,
                        f
                ),
                f -> narrate(
                        scan,
                        levels,
                        scanFrames,
                        reduceFrames,
                        f
                ),
                result,
                complexity,
                -1,
                frames,
                info
        );
    }

    private void draw(
            long[] data,
            BlellochScan.Trace scan,
            List<long[]> levels,
            int scanFrames,
            int frame
    ) {

        if (frame < scanFrames) {

            BlellochScan.Step s =
                    scan.steps().get(frame);

            long[] snap =
                    s.snapshot();

            ArrayVisualizer.Cell[] row =
                    new ArrayVisualizer.Cell[snap.length];

            java.util.Set<Integer> touched =
                    new java.util.HashSet<>();

            for (int t : s.touched()) {
                touched.add(t);
            }

            for (int i = 0; i < snap.length; i++) {

                CellState state =
                        i >= data.length
                                ? CellState.MUTED
                                : touched.contains(i)
                                ? CellState.ACTIVE
                                : CellState.NORMAL;

                row[i] =
                        ArrayVisualizer.cell(
                                String.valueOf(snap[i]),
                                state
                        );
            }

            // FIXED
            visualizer.render(
                    List.of(
                            s.phase()
                                    + (s.level() >= 0
                                    ? " d=" + s.level()
                                    : "")
                    ),
                    List.<ArrayVisualizer.Cell[]>of(row),
                    -1
            );

        } else if (frame < scanFrames + levels.size()) {

            long[] level =
                    levels.get(frame - scanFrames);

            ArrayVisualizer.Cell[] row =
                    new ArrayVisualizer.Cell[level.length];

            for (int i = 0; i < level.length; i++) {

                row[i] =
                        ArrayVisualizer.cell(
                                String.valueOf(level[i]),
                                level.length == 1
                                        ? CellState.MATCH
                                        : CellState.NORMAL
                        );
            }

            // FIXED
            visualizer.render(
                    List.of(
                            "Reduction level "
                                    + (frame - scanFrames)
                    ),
                    List.<ArrayVisualizer.Cell[]>of(row),
                    -1
            );

        } else {

            ArrayVisualizer.Cell[] row = {
                    ArrayVisualizer.cell(
                            "done",
                            CellState.MATCH
                    )
            };

            // FIXED
            visualizer.render(
                    List.of("Large-scale reduce"),
                    List.<ArrayVisualizer.Cell[]>of(row),
                    -1
            );
        }
    }

    private static String narrate(
            BlellochScan.Trace scan,
            List<long[]> levels,
            int scanFrames,
            int reduceFrames,
            int frame
    ) {

        if (frame < scanFrames) {

            BlellochScan.Step s =
                    scan.steps().get(frame);

            return switch (s.phase()) {

                case "input" ->
                        "Input, padded to a power of two with zeros.";

                case "up-sweep" ->
                        "Up-sweep level "
                                + s.level()
                                + ": every pair combines into a partial sum. "
                                + "All pairs at this level are independent – "
                                + "one parallel step.";

                case "root" ->
                        "The last element is set to 0 "
                                + "(the identity), which is where the "
                                + "down-sweep starts.";

                default ->
                        "Down-sweep level "
                                + s.level()
                                + ": each node passes its old value left "
                                + "and adds it in, producing the exclusive "
                                + "prefix sums.";
            };
        }

        if (frame < scanFrames + reduceFrames) {

            int level =
                    frame - scanFrames;

            return level == 0
                    ? "The small example illustrates the same reduction "
                    + "tree used on 2,000,000 values below: level 0 is "
                    + "the raw input."
                    : "Level "
                    + level
                    + ": "
                    + levels.get(level - 1).length
                    + " values combine pairwise into "
                    + levels.get(level).length
                    + ".";
        }

        return "The 2,000,000-value sum ran on Java's Fork/Join common pool, "
                + "splitting the array until chunks are small enough to sum "
                + "directly, then combining results back up the tree – the "
                + "same shape as the small example above.";
    }
}