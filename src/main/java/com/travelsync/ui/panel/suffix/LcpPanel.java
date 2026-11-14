package com.travelsync.ui.panel.suffix;

import com.travelsync.algorithms.suffix.LCPArrayBuilder;
import com.travelsync.algorithms.suffix.SuffixArrayBuilder;
import com.travelsync.ui.AlgorithmPanel;
import com.travelsync.ui.Execution;
import com.travelsync.ui.Ui;
import com.travelsync.util.ValidationException;
import com.travelsync.visualization.CellState;
import com.travelsync.visualization.RowTableVisualizer;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** LCP / Kasai panel: compares neighbouring suffixes to find phrases shared inside a description. */
public final class LcpPanel extends AlgorithmPanel {

    private static final int MAX_LENGTH = 120;

    private final TextArea textArea = new TextArea();
    private final RowTableVisualizer table = new RowTableVisualizer("Rank", "Start index", "Suffix", "LCP with previous");

    public LcpPanel() {
        textArea.setPrefRowCount(2);
        textArea.setWrapText(true);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.add(Ui.label("Document", "kv-key"), 0, 0);
        grid.add(textArea, 1, 0);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        assemble("LCP array \u2013 Kasai",
                "Travel problem: find common phrases inside or between destination descriptions. lcp[i] is the length of the longest "
                        + "common prefix of two neighbouring suffixes in the suffix array; the largest value marks the longest repeated "
                        + "phrase. Kasai's algorithm computes all values in O(n).",
                grid, table);
        loadDemo();
    }

    @Override
    protected void loadDemo() {
        textArea.setText("Kyoto temples Kyoto gardens Kyoto geisha districts");
    }

    @Override
    protected Supplier<Execution> prepare() throws ValidationException {
        String text = textArea.getText();
        if (text == null || text.isEmpty()) throw new ValidationException("Please enter the document text first.");
        if (text.length() > MAX_LENGTH) {
            throw new ValidationException("For a readable table please use at most " + MAX_LENGTH + " characters.");
        }
        return () -> execute(text);
    }

    private Execution execute(String text) {
        int[] sa = SuffixArrayBuilder.build(text);
        int[] lcp = LCPArrayBuilder.build(text, sa);
        int n = text.length();
        int best = 0;
        for (int v : lcp) best = Math.max(best, v);
        String repeated = LCPArrayBuilder.longestRepeatedSubstring(text);
        int frames = n + 1;
        int maxLcp = best;
        String result = repeated.isEmpty() ? "No repeated substring." : "Longest repeated substring: \"" + repeated + "\" (" + best + " characters)";
        return new Execution(frames, f -> draw(text, sa, lcp, maxLcp, f), f -> narrate(text, sa, lcp, maxLcp, f), result,
                "Kasai: Time O(n) = O(" + n + ")   |   Space O(n) for the rank and LCP arrays", -1, frames,
                "The suffix array was built first (prefix doubling), then LCP values were derived from it.");
    }

    private void draw(String text, int[] sa, int[] lcp, int maxLcp, int frame) {
        int n = sa.length;
        List<String[]> rows = new ArrayList<>();
        Map<Integer, CellState> states = new HashMap<>();
        for (int r = 0; r < n; r++) {
            rows.add(new String[]{String.valueOf(r), String.valueOf(sa[r]), SuffixArrayPanel.preview(text, sa[r]),
                    r == 0 ? "\u2013" : String.valueOf(lcp[r])});
        }
        if (frame >= 1 && frame <= n - 1) {
            states.put(frame - 1, CellState.ACTIVE);
            states.put(frame, CellState.HIGHLIGHT);
        } else if (frame == n && maxLcp > 0) {
            for (int r = 1; r < n; r++) {
                if (lcp[r] == maxLcp) {
                    states.put(r - 1, CellState.MATCH);
                    states.put(r, CellState.MATCH);
                }
            }
        }
        table.show(rows, states);
        if (frame >= 1 && frame <= n - 1) table.scrollTo(frame);
    }

    private static String narrate(String text, int[] sa, int[] lcp, int maxLcp, int frame) {
        int n = sa.length;
        if (frame == 0) return "The suffixes are sorted. Neighbouring suffixes now share long prefixes, which Kasai's algorithm measures.";
        if (frame <= n - 1) {
            return "lcp[" + frame + "] = " + lcp[frame] + ": the suffixes at indices " + sa[frame - 1] + " and " + sa[frame]
                    + " share their first " + lcp[frame] + " character(s).";
        }
        return maxLcp == 0 ? "No two suffixes share a prefix, so nothing repeats."
                : "The largest LCP value is " + maxLcp + ": the rows marked green share the longest repeated phrase.";
    }
}
