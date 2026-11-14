package com.travelsync.ui.panel.dp;

import com.travelsync.algorithms.dp.DamerauLevenshteinCalculator;
import com.travelsync.algorithms.dp.LevenshteinCalculator;
import com.travelsync.data.AppDataStore;
import com.travelsync.model.Destination;
import com.travelsync.ui.AlgorithmPanel;
import com.travelsync.ui.Execution;
import com.travelsync.ui.Ui;
import com.travelsync.util.ValidationException;
import com.travelsync.visualization.CellState;
import com.travelsync.visualization.MatrixVisualizer;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.function.Supplier;

/** Edit-distance panel: corrects misspelled destination / hotel names, filling the DP matrix cell by cell. */
public final class EditDistancePanel extends AlgorithmPanel {

    static final String LEV = "Edit Distance";
    static final String DAM = "Damerau-Levenshtein";
    private static final int MAX_LENGTH = 16;

    private final TextField typedField = new TextField();
    private final TextField correctField = new TextField();
    private final ComboBox<String> method = new ComboBox<>();
    private final MatrixVisualizer visualizer = new MatrixVisualizer();

    public EditDistancePanel() {
        method.getItems().addAll(LEV, DAM);
        method.setValue(LEV);
        Button suggest = Ui.button("Suggest closest destination", "btn-secondary",
                "Find the destination name with the smallest Damerau-Levenshtein distance to the typed name");
        suggest.setOnAction(e -> {
            String typed = typedField.getText() == null ? "" : typedField.getText().trim();
            String best = null;
            int bestDistance = Integer.MAX_VALUE;
            for (Destination d : AppDataStore.getInstance().getDestinations()) {
                int dist = DamerauLevenshteinCalculator.compute(typed.toLowerCase(), d.getName().toLowerCase()).distance();
                if (dist < bestDistance) { bestDistance = dist; best = d.getName(); }
            }
            if (best != null) correctField.setText(best);
        });
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(Ui.label("Typed name", "kv-key"), 0, 0);
        grid.add(typedField, 1, 0);
        grid.add(suggest, 2, 0);
        grid.add(Ui.label("Correct name", "kv-key"), 0, 1);
        grid.add(correctField, 1, 1);
        grid.add(Ui.label("Variant", "kv-key"), 0, 2);
        grid.add(method, 1, 2);
        GridPane.setHgrow(typedField, Priority.ALWAYS);
        assemble("Edit distance \u2013 Levenshtein / Damerau-Levenshtein",
                "Travel problem: a traveler mistypes a destination or hotel name. The DP matrix cell (i, j) holds the fewest edits needed "
                        + "to turn the first i typed characters into the first j correct ones. Damerau-Levenshtein also counts an adjacent "
                        + "swap (\"Barcleona\") as a single edit.",
                grid, visualizer);
        loadDemo();
    }

    @Override
    public void prefer(String algorithm) {
        method.setValue(DAM.equals(algorithm) ? DAM : LEV);
    }

    @Override
    protected void loadDemo() {
        typedField.setText("Barcleona");
        correctField.setText("Barcelona");
    }

    @Override
    protected Supplier<Execution> prepare() throws ValidationException {
        String a = typedField.getText() == null ? "" : typedField.getText().trim();
        String b = correctField.getText() == null ? "" : correctField.getText().trim();
        if (a.isEmpty() || b.isEmpty()) throw new ValidationException("Please enter both the typed and the correct name.");
        if (a.length() > MAX_LENGTH || b.length() > MAX_LENGTH) {
            throw new ValidationException("Please use at most " + MAX_LENGTH + " characters per name so the matrix stays readable.");
        }
        boolean damerau = DAM.equals(method.getValue());
        return () -> execute(a, b, damerau);
    }

    private Execution execute(String a, String b, boolean damerau) {
        int[][] t = damerau ? DamerauLevenshteinCalculator.compute(a, b).table() : LevenshteinCalculator.compute(a, b).table();
        LevenshteinCalculator.Result lev = LevenshteinCalculator.compute(a, b);
        int n = a.length();
        int m = b.length();
        boolean[][] path = path(t, a, b, damerau);
        int frames = n * m + 1;
        String result = "Distance = " + t[n][m] + (damerau ? " (Damerau-Levenshtein)" : " (Levenshtein)")
                + (damerau ? "\nPlain Levenshtein distance = " + lev.distance()
                : "\nEdit script: " + (lev.operations().isEmpty() ? "no edits needed" : String.join("; ", lev.operations())));
        String complexity = "Time O(n\u00B7m) = O(" + n + "\u00B7" + m + ") = " + (long) n * m + " cells   |   Space O(n\u00B7m)";
        return new Execution(frames, f -> draw(a, b, t, path, f), f -> narrate(a, b, t, damerau, f), result, complexity,
                (long) n * m, frames, "The green cells in the last step mark one cheapest sequence of edits.");
    }

    private void draw(String a, String b, int[][] t, boolean[][] path, int frame) {
        int n = a.length();
        int m = b.length();
        String[] rowHeaders = new String[n + 1];
        String[] colHeaders = new String[m + 1];
        rowHeaders[0] = "\u03B5";
        colHeaders[0] = "\u03B5";
        for (int i = 1; i <= n; i++) rowHeaders[i] = String.valueOf(a.charAt(i - 1));
        for (int j = 1; j <= m; j++) colHeaders[j] = String.valueOf(b.charAt(j - 1));
        String[][] values = new String[n + 1][m + 1];
        CellState[][] states = new CellState[n + 1][m + 1];
        boolean last = frame == n * m;
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= m; j++) {
                boolean boundary = i == 0 || j == 0;
                int order = boundary ? -1 : (i - 1) * m + (j - 1);
                if (boundary || order <= frame || last) {
                    values[i][j] = String.valueOf(t[i][j]);
                    states[i][j] = boundary ? CellState.MUTED : CellState.NORMAL;
                    if (last && path[i][j]) states[i][j] = CellState.MATCH;
                    if (!last && order == frame) states[i][j] = CellState.ACTIVE;
                }
            }
        }
        int focusCol = last ? m : (frame % Math.max(1, m)) + 1;
        visualizer.show(rowHeaders, colHeaders, values, states, focusCol);
    }

    private static String narrate(String a, String b, int[][] t, boolean damerau, int frame) {
        int n = a.length();
        int m = b.length();
        if (frame == n * m) return "Filled. The distance is " + t[n][m] + "; the green path shows one cheapest series of edits from the corner back to the start.";
        int i = frame / m + 1;
        int j = frame % m + 1;
        char x = a.charAt(i - 1);
        char y = b.charAt(j - 1);
        int v = t[i][j];
        String head = "Cell (" + i + ", " + j + "): '" + x + "' vs '" + y + "' \u2192 ";
        if (x == y) return head + "the characters are equal, so no edit is needed: copy the diagonal value " + t[i - 1][j - 1] + ".";
        int viaSub = t[i - 1][j - 1] + 1;
        int viaDel = t[i - 1][j] + 1;
        int viaIns = t[i][j - 1] + 1;
        if (damerau && i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2) && a.charAt(i - 2) == b.charAt(j - 1)
                && v == t[i - 2][j - 2] + 1 && v < Math.min(viaSub, Math.min(viaDel, viaIns))) {
            return head + "the last two characters are swapped, so one transposition from cell (" + (i - 2) + ", " + (j - 2) + ") costs " + v + ".";
        }
        return head + "different, so take 1 + min(substitute " + t[i - 1][j - 1] + ", delete " + t[i - 1][j] + ", insert "
                + t[i][j - 1] + ") = " + v + ".";
    }

    private static boolean[][] path(int[][] t, String a, String b, boolean damerau) {
        int i = a.length();
        int j = b.length();
        boolean[][] p = new boolean[i + 1][j + 1];
        p[i][j] = true;
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && a.charAt(i - 1) == b.charAt(j - 1) && t[i][j] == t[i - 1][j - 1]) {
                i--; j--;
            } else if (damerau && i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2) && a.charAt(i - 2) == b.charAt(j - 1)
                    && t[i][j] == t[i - 2][j - 2] + 1) {
                i -= 2; j -= 2;
            } else if (i > 0 && j > 0 && t[i][j] == t[i - 1][j - 1] + 1) {
                i--; j--;
            } else if (i > 0 && t[i][j] == t[i - 1][j] + 1) {
                i--;
            } else {
                j--;
            }
            p[i][j] = true;
        }
        return p;
    }
}
