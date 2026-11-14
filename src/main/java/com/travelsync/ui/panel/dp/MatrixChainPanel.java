package com.travelsync.ui.panel.dp;

import com.travelsync.algorithms.dp.MatrixChainOptimizer;
import com.travelsync.ui.AlgorithmPanel;
import com.travelsync.ui.Execution;
import com.travelsync.ui.Ui;
import com.travelsync.util.ValidationException;
import com.travelsync.visualization.CellState;
import com.travelsync.visualization.MatrixVisualizer;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Matrix-chain panel: orders a chain of travel-data matrix operations, filling the cost table by chain length. */
public final class MatrixChainPanel extends AlgorithmPanel {

    private final TextField dimsField = new TextField();
    private final MatrixVisualizer visualizer = new MatrixVisualizer();

    public MatrixChainPanel() {
        dimsField.setPromptText("Dimensions p0, p1, ..., pn (matrix i is p[i-1] x p[i]), e.g. 40, 20, 30, 10, 30");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.add(Ui.label("Dimensions", "kv-key"), 0, 0);
        grid.add(dimsField, 1, 0);
        GridPane.setHgrow(dimsField, Priority.ALWAYS);
        assemble("Matrix Chain Multiplication",
                "Travel problem: booking and demand analytics chain several matrix operations (e.g. destination \u00D7 month \u00D7 segment). "
                        + "The order of multiplication changes the work enormously; cost[i][j] is the cheapest way to multiply matrices i..j.",
                grid, visualizer);
        loadDemo();
    }

    @Override
    protected void loadDemo() {
        dimsField.setText("40, 20, 30, 10, 30");
    }

    @Override
    protected Supplier<Execution> prepare() throws ValidationException {
        String raw = dimsField.getText();
        if (raw == null || raw.isBlank()) throw new ValidationException("Please enter the matrix dimensions.");
        String[] parts = raw.split(",");
        int[] dims = new int[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) {
                dims[i] = Integer.parseInt(parts[i].trim());
                if (dims[i] <= 0) throw new ValidationException("All dimensions must be positive whole numbers.");
            }
        } catch (NumberFormatException ex) {
            throw new ValidationException("Dimensions must be whole numbers separated by commas, e.g. 40, 20, 30, 10, 30.");
        }
        int matrices = dims.length - 1;
        if (matrices < 2 || matrices > 9) throw new ValidationException("Please enter between 3 and 10 dimensions (2 to 9 matrices).");
        return () -> execute(dims);
    }

    private Execution execute(int[] dims) {
        int n = dims.length - 1;
        MatrixChainOptimizer.Result r = MatrixChainOptimizer.compute(dims);
        List<int[]> order = new ArrayList<>();
        for (int len = 2; len <= n; len++) {
            for (int i = 1; i <= n - len + 1; i++) order.add(new int[]{i, i + len - 1});
        }
        int frames = order.size() + 1;
        long naive = 0;
        for (int i = 1; i < n; i++) naive += (long) dims[0] * dims[i] * dims[i + 1];
        String result = "Minimum scalar multiplications: " + r.minMultiplications() + "\nBest order: " + r.parenthesization();
        String complexity = "Time O(n\u00B3) = O(" + n + "\u00B3)   |   Space O(n\u00B2)";
        String info = "Multiplying strictly left to right would cost " + naive + " scalar multiplications.";
        return new Execution(frames, f -> draw(n, r, order, f), f -> narrate(dims, r, order, f), result, complexity,
                (long) order.size(), frames, info);
    }

    private void draw(int n, MatrixChainOptimizer.Result r, List<int[]> order, int frame) {
        String[] headers = new String[n];
        for (int i = 0; i < n; i++) headers[i] = "M" + (i + 1);
        String[][] values = new String[n][n];
        CellState[][] states = new CellState[n][n];
        for (int i = 0; i < n; i++) {
            values[i][i] = "0";
            states[i][i] = CellState.MUTED;
        }
        for (int k = 0; k < frame; k++) {
            int[] c = order.get(k);
            values[c[0] - 1][c[1] - 1] = String.valueOf(r.costTable()[c[0]][c[1]]);
            states[c[0] - 1][c[1] - 1] = k == frame - 1 ? CellState.ACTIVE : CellState.NORMAL;
        }
        if (frame == order.size()) states[0][n - 1] = CellState.MATCH;
        int focus = frame == 0 ? 0 : order.get(frame - 1)[1] - 1;
        visualizer.show(headers, headers, values, states, focus);
    }

    private static String narrate(int[] dims, MatrixChainOptimizer.Result r, List<int[]> order, int frame) {
        if (frame == 0) return "Chains of length 1 need no multiplication, so the diagonal is 0. Now chains of length 2, 3, \u2026 are solved in turn.";
        int[] c = order.get(frame - 1);
        int i = c[0];
        int j = c[1];
        int k = r.splitTable()[i][j];
        long left = r.costTable()[i][k];
        long right = r.costTable()[k + 1][j];
        long join = (long) dims[i - 1] * dims[k] * dims[j];
        String text = "cost[" + i + "][" + j + "]: the best split is after M" + k + ": " + left + " + " + right + " + "
                + dims[i - 1] + "\u00B7" + dims[k] + "\u00B7" + dims[j] + " = " + r.costTable()[i][j] + ".";
        return frame == order.size() ? text + " Done: the answer is the top-right cell, order " + r.parenthesization() + "." : text;
    }
}
