package com.travelsync.ui.panel.suffix;

import com.travelsync.algorithms.suffix.LCPArrayBuilder;
import com.travelsync.algorithms.suffix.SAISBuilder;
import com.travelsync.algorithms.suffix.SuffixArrayBuilder;
import com.travelsync.data.AppDataStore;
import com.travelsync.model.Destination;
import com.travelsync.ui.AlgorithmPanel;
import com.travelsync.ui.Execution;
import com.travelsync.ui.Ui;
import com.travelsync.util.ValidationException;
import com.travelsync.visualization.CellState;
import com.travelsync.visualization.RowTableVisualizer;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Suffix Array panel: builds the index of a travel description with prefix doubling or SA-IS and reveals it rank by rank. */
public final class SuffixArrayPanel extends AlgorithmPanel {

    static final String DOUBLING = "Suffix Array";
    static final String SAIS = "SA-IS";
    private static final int MAX_LENGTH = 120;

    private final TextArea textArea = new TextArea();
    private final ComboBox<String> method = new ComboBox<>();
    private final RowTableVisualizer table = new RowTableVisualizer("Rank", "Start index", "Suffix", "LCP with previous");

    public SuffixArrayPanel() {
        textArea.setPrefRowCount(2);
        textArea.setWrapText(true);
        method.getItems().addAll(DOUBLING, SAIS);
        method.setValue(DOUBLING);
        Button useData = Ui.button("Use a destination description", "btn-secondary", "Index a description from the current data");
        useData.setOnAction(e -> {
            List<Destination> list = AppDataStore.getInstance().getDestinations();
            if (!list.isEmpty()) textArea.setText(list.get(0).getDescription());
        });
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(Ui.label("Document", "kv-key"), 0, 0);
        grid.add(textArea, 1, 0);
        grid.add(useData, 2, 0);
        grid.add(Ui.label("Construction", "kv-key"), 0, 1);
        grid.add(method, 1, 1);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        assemble("Suffix Array \u2013 doubling and SA-IS",
                "Travel problem: index hotel, destination and travel descriptions for fast substring search. The suffix array lists "
                        + "the starting positions of all suffixes in sorted order; prefix doubling builds it in O(n log n), SA-IS in O(n).",
                grid, table);
        loadDemo();
    }

    @Override
    public void prefer(String algorithm) {
        method.setValue(SAIS.equals(algorithm) ? SAIS : DOUBLING);
    }

    @Override
    protected void loadDemo() {
        textArea.setText("Historic temples, gardens and geisha districts.");
    }

    @Override
    protected Supplier<Execution> prepare() throws ValidationException {
        String text = textArea.getText();
        if (text == null || text.isEmpty()) throw new ValidationException("Please enter the document text first.");
        if (text.length() > MAX_LENGTH) {
            throw new ValidationException("For a readable table please use at most " + MAX_LENGTH + " characters.");
        }
        boolean sais = SAIS.equals(method.getValue());
        return () -> execute(text, sais);
    }

    private Execution execute(String text, boolean sais) {
        int[] sa = sais ? SAISBuilder.build(text) : SuffixArrayBuilder.build(text);
        int[] other = sais ? SuffixArrayBuilder.build(text) : SAISBuilder.build(text);
        int[] lcp = LCPArrayBuilder.build(text, sa);
        int n = text.length();
        String complexity = sais ? "SA-IS: Time O(n) = O(" + n + ")   |   Space O(n)"
                : "Prefix doubling: Time O(n log n) = O(" + n + " \u00B7 log " + n + ")   |   Space O(n)";
        String result = "Suffix array = " + (n <= 24 ? Arrays.toString(sa) : "[" + n + " entries]")
                + "\nLongest repeated substring: \"" + LCPArrayBuilder.longestRepeatedSubstring(text) + "\"";
        String info = "Cross-check: " + (sais ? "prefix doubling" : "SA-IS") + " produced "
                + (Arrays.equals(sa, other) ? "an identical" : "a DIFFERENT") + " suffix array.";
        return new Execution(n, f -> draw(text, sa, lcp, f), f -> narrate(text, sa, lcp, f), result, complexity,
                -1, n, info);
    }

    private void draw(String text, int[] sa, int[] lcp, int frame) {
        List<String[]> rows = new ArrayList<>();
        Map<Integer, CellState> states = new HashMap<>();
        for (int r = 0; r <= frame; r++) {
            rows.add(new String[]{String.valueOf(r), String.valueOf(sa[r]), preview(text, sa[r]), r == 0 ? "\u2013" : String.valueOf(lcp[r])});
            states.put(r, r == frame ? CellState.HIGHLIGHT : CellState.NORMAL);
        }
        table.show(rows, states);
        table.scrollTo(frame);
    }

    private static String narrate(String text, int[] sa, int[] lcp, int frame) {
        String base = "Rank " + frame + ": the suffix starting at index " + sa[frame] + " is \"" + preview(text, sa[frame]) + "\".";
        return frame == 0 ? base + " It is the lexicographically smallest suffix."
                : base + " It shares " + lcp[frame] + " leading character(s) with the previous suffix.";
    }

    static String preview(String text, int start) {
        String s = text.substring(start);
        return s.length() > 32 ? s.substring(0, 32) + "\u2026" : s;
    }
}
