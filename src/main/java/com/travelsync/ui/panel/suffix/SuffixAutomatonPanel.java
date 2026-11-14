package com.travelsync.ui.panel.suffix;

import com.travelsync.algorithms.suffix.SuffixAutomaton;
import com.travelsync.ui.AlgorithmPanel;
import com.travelsync.ui.Execution;
import com.travelsync.ui.Ui;
import com.travelsync.util.ValidationException;
import com.travelsync.visualization.ArrayVisualizer;
import com.travelsync.visualization.CellState;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Suffix Automaton panel: finds the longest phrase shared by two travel descriptions. */
public final class SuffixAutomatonPanel extends AlgorithmPanel {

    private static final int MAX_LENGTH = 90;

    private final TextArea sourceArea = new TextArea();
    private final TextArea otherArea = new TextArea();
    private final ArrayVisualizer visualizer = new ArrayVisualizer();

    public SuffixAutomatonPanel() {
        sourceArea.setPrefRowCount(2);
        otherArea.setPrefRowCount(2);
        sourceArea.setWrapText(true);
        otherArea.setWrapText(true);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(Ui.label("Description A", "kv-key"), 0, 0);
        grid.add(sourceArea, 1, 0);
        grid.add(Ui.label("Description B", "kv-key"), 0, 1);
        grid.add(otherArea, 1, 1);
        GridPane.setHgrow(sourceArea, Priority.ALWAYS);
        GridPane.setHgrow(otherArea, Priority.ALWAYS);
        assemble("Suffix Automaton",
                "Travel problem: substring-based search and similarity between destination descriptions. The automaton built from "
                        + "description A accepts every substring of A; description B is walked through it and suffix links jump back "
                        + "on a missing transition, so the longest shared phrase is found in O(|B|).",
                grid, visualizer);
        loadDemo();
    }

    @Override
    protected void loadDemo() {
        sourceArea.setText("Historic temples, gardens and geisha districts.");
        otherArea.setText("Explore historic temples, gardens and quiet shrines.");
    }

    @Override
    protected Supplier<Execution> prepare() throws ValidationException {
        String a = sourceArea.getText();
        String b = otherArea.getText();
        if (a == null || a.isEmpty() || b == null || b.isEmpty()) {
            throw new ValidationException("Please enter both descriptions.");
        }
        if (a.length() > MAX_LENGTH || b.length() > MAX_LENGTH) {
            throw new ValidationException("Please use at most " + MAX_LENGTH + " characters per description.");
        }
        return () -> execute(a, b);
    }

    private Execution execute(String a, String b) {
        SuffixAutomaton sam = new SuffixAutomaton(a);
        List<SuffixAutomaton.WalkStep> steps = sam.walk(b);
        String lcs = sam.longestCommonSubstring(b);
        String result = lcs.isEmpty() ? "No common substring." : "Longest common substring: \"" + lcs + "\" (" + lcs.length() + " characters)";
        String complexity = "Build O(|A|) = O(" + a.length() + ")   |   Query O(|B|) = O(" + b.length() + ")   |   Space O(|A|)";
        String info = "The automaton has " + sam.getStateCount() + " states (at most 2|A| \u2212 1 = " + Math.max(1, 2 * a.length() - 1) + ").";
        return new Execution(steps.size(), f -> draw(b, steps, f), f -> narrate(b, steps, f), result, complexity, -1,
                steps.size(), info);
    }

    private void draw(String b, List<SuffixAutomaton.WalkStep> steps, int frame) {
        int n = b.length();
        int best = 0;
        for (int i = 0; i <= frame; i++) best = Math.max(best, steps.get(i).length());
        SuffixAutomaton.WalkStep cur = steps.get(frame);
        ArrayVisualizer.Cell[] index = new ArrayVisualizer.Cell[n];
        ArrayVisualizer.Cell[] chars = new ArrayVisualizer.Cell[n];
        ArrayVisualizer.Cell[] lengths = new ArrayVisualizer.Cell[n];
        for (int i = 0; i < n; i++) {
            index[i] = ArrayVisualizer.cell(String.valueOf(i), CellState.MUTED);
            CellState st = CellState.NORMAL;
            if (i <= frame && i > cur.index() - cur.length() && i <= cur.index()) {
                st = cur.length() == best ? CellState.MATCH : CellState.WINDOW;
            }
            if (i == cur.index()) st = cur.length() == 0 ? CellState.MISMATCH : CellState.ACTIVE;
            chars[i] = ArrayVisualizer.cell(String.valueOf(b.charAt(i)), st);
            lengths[i] = i <= frame ? ArrayVisualizer.cell(String.valueOf(steps.get(i).length()),
                    i == cur.index() ? CellState.HIGHLIGHT : CellState.MUTED) : null;
        }
        visualizer.render(List.of("Index", "Description B", "Match length"), new ArrayList<>(List.of(index, chars, lengths)), cur.index());
    }

    private static String narrate(String b, List<SuffixAutomaton.WalkStep> steps, int frame) {
        SuffixAutomaton.WalkStep s = steps.get(frame);
        int best = 0;
        for (int i = 0; i <= frame; i++) best = Math.max(best, steps.get(i).length());
        return "Read '" + b.charAt(s.index()) + "': " + (s.fellBack() ? "no transition from the current state, so suffix links were followed; " : "")
                + "the current common phrase has length " + s.length() + ". Best so far: " + best + ".";
    }
}
