package com.travelsync.ui.panel.string;

import com.travelsync.algorithms.string.StringMatchStep;
import com.travelsync.algorithms.string.ZFunctionMatcher;
import com.travelsync.ui.AlgorithmPanel;
import com.travelsync.ui.Execution;
import com.travelsync.ui.Ui;
import com.travelsync.util.ValidationException;
import com.travelsync.visualization.ArrayVisualizer;
import com.travelsync.visualization.CellState;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Z-Function panel: builds the Z-array of pattern + separator + text and reports repeated phrases. */
public final class ZFunctionPanel extends AlgorithmPanel {

    private final TextField textField = new TextField();
    private final TextField patternField = new TextField();
    private final ArrayVisualizer visualizer = new ArrayVisualizer();

    public ZFunctionPanel() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(Ui.label("Text", "kv-key"), 0, 0);
        grid.add(textField, 1, 0);
        grid.add(Ui.label("Phrase", "kv-key"), 0, 1);
        grid.add(patternField, 1, 1);
        GridPane.setHgrow(textField, Priority.ALWAYS);
        assemble("Z-Function",
                "Travel problem: detect repeated phrases in destination descriptions. z[i] is the length of the longest "
                        + "substring starting at i that is also a prefix of the string; a value equal to the phrase length "
                        + "marks an occurrence.",
                grid, visualizer);
        loadDemo();
    }

    @Override
    protected void loadDemo() {
        textField.setText("Historic temples and gardens. Historic temples and shrines.");
        patternField.setText("Historic temples");
    }

    @Override
    protected Supplier<Execution> prepare() throws ValidationException {
        String text = StringInputs.requireText(textField.getText(), "text");
        String pattern = StringInputs.requireText(patternField.getText(), "phrase");
        if (pattern.length() > text.length()) {
            throw new ValidationException("The phrase is longer than the text, so it cannot occur in it.");
        }
        return () -> execute(text, pattern);
    }

    private Execution execute(String text, String pattern) {
        List<StringMatchStep> steps = ZFunctionMatcher.trace(text, pattern);
        List<Integer> matches = ZFunctionMatcher.search(text, pattern);
        String combined = pattern + "\u0001" + text;
        long comparisons = steps.stream().filter(s -> s.getType() == StringMatchStep.Type.TABLE_BUILD).count();
        String result = matches.isEmpty() ? "No occurrence found." : matches.size() + " occurrence(s) at index " + matches;
        String complexity = "Time O(n) = O(" + combined.length() + ")   |   Space O(n) for the Z-array";
        String info = "Combined string = phrase + separator + text (" + combined.length() + " characters).";
        return new Execution(steps.size(), f -> draw(combined, pattern.length(), steps, f),
                f -> steps.get(f).getLabel(), result, complexity, comparisons, steps.size(), info);
    }

    private void draw(String combined, int m, List<StringMatchStep> steps, int frame) {
        int n = combined.length();
        int[] z = null;
        int maxComputed = 0;
        boolean[] found = new boolean[n];
        for (int i = 0; i <= frame; i++) {
            StringMatchStep s = steps.get(i);
            if (s.getAuxiliaryArraySnapshot() != null) z = s.getAuxiliaryArraySnapshot();
            if (s.getType() == StringMatchStep.Type.TABLE_BUILD) maxComputed = Math.max(maxComputed, s.getTextIndex());
            if (s.getType() == StringMatchStep.Type.FULL_MATCH) {
                for (int k = 0; k < m; k++) found[s.getTextIndex() + m + 1 + k] = true;
            }
        }
        StringMatchStep current = steps.get(frame);
        int cur = current.getType() == StringMatchStep.Type.INFO ? -1
                : current.getType() == StringMatchStep.Type.FULL_MATCH ? current.getTextIndex() + m + 1
                : current.getTextIndex();
        ArrayVisualizer.Cell[] index = new ArrayVisualizer.Cell[n];
        ArrayVisualizer.Cell[] chars = new ArrayVisualizer.Cell[n];
        ArrayVisualizer.Cell[] zRow = new ArrayVisualizer.Cell[n];
        for (int i = 0; i < n; i++) {
            index[i] = ArrayVisualizer.cell(String.valueOf(i), CellState.MUTED);
            char c = combined.charAt(i);
            CellState state = i < m ? CellState.WINDOW : found[i] ? CellState.DONE : CellState.NORMAL;
            if (i == m) state = CellState.MUTED;
            if (i == cur) state = current.getType() == StringMatchStep.Type.FULL_MATCH ? CellState.MATCH : CellState.ACTIVE;
            chars[i] = ArrayVisualizer.cell(i == m ? "#" : String.valueOf(c), state);
            String zText = (z == null || i > maxComputed || i == 0) ? "\u00B7" : String.valueOf(z[i]);
            zRow[i] = ArrayVisualizer.cell(zText, i == cur ? CellState.HIGHLIGHT : CellState.MUTED);
        }
        visualizer.render(List.of("Index", "Phrase # text", "Z array"), new ArrayList<>(List.of(index, chars, zRow)), Math.max(cur, 0));
    }
}
