package com.travelsync.ui.panel.string;

import com.travelsync.algorithms.string.AhoCorasickMatcher;
import com.travelsync.algorithms.string.StringMatchStep;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Aho-Corasick panel: detects several destination / service keywords in one pass over a traveler's request. */
public final class AhoCorasickPanel extends AlgorithmPanel {

    private final TextField textField = new TextField();
    private final TextField patternsField = new TextField();
    private final ArrayVisualizer visualizer = new ArrayVisualizer();

    public AhoCorasickPanel() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(Ui.label("Text", "kv-key"), 0, 0);
        grid.add(textField, 1, 0);
        grid.add(Ui.label("Keywords", "kv-key"), 0, 1);
        grid.add(patternsField, 1, 1);
        GridPane.setHgrow(textField, Priority.ALWAYS);
        GridPane.setHgrow(patternsField, Priority.ALWAYS);
        assemble("Aho-Corasick",
                "Travel problem: detect many destination and service keywords in a single pass over a traveler's request "
                        + "or a travel document. A trie of all keywords is extended with failure links, so the text is read "
                        + "exactly once no matter how many keywords there are.",
                grid, visualizer);
        loadDemo();
    }

    @Override
    protected void loadDemo() {
        textField.setText("beach resort in bali with spa and airport transfer near a temple");
        patternsField.setText("beach, resort, spa, transfer, temple");
    }

    @Override
    protected Supplier<Execution> prepare() throws ValidationException {
        String text = StringInputs.requireText(textField.getText(), "text");
        String raw = StringInputs.requireText(patternsField.getText(), "keywords (comma separated)");
        List<String> patterns = Arrays.stream(raw.split(",")).map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
        if (patterns.isEmpty()) throw new ValidationException("Please enter at least one keyword.");
        return () -> execute(text, patterns);
    }

    private Execution execute(String text, List<String> patterns) {
        AhoCorasickMatcher matcher = new AhoCorasickMatcher(patterns);
        List<StringMatchStep> steps = matcher.trace(text);
        Map<String, List<Integer>> found = matcher.search(text);
        long transitions = steps.stream().filter(s -> s.getType() == StringMatchStep.Type.STATE_TRANSITION).count();
        StringBuilder result = new StringBuilder();
        if (found.isEmpty()) {
            result.append("No keyword found.");
        } else {
            found.forEach((p, ends) -> result.append(p).append(" \u2192 starts ")
                    .append(ends.stream().map(e -> e - p.length() + 1).toList()).append("\n"));
        }
        String complexity = "Build O(\u03A3|keywords|)   |   Search O(n + matches) = O(" + text.length() + " + "
                + found.values().stream().mapToInt(List::size).sum() + ")";
        String info = "Automaton has " + matcher.getStateCount() + " states for " + patterns.size() + " keyword(s).";
        return new Execution(steps.size(), f -> draw(text, patterns, steps, f), f -> steps.get(f).getLabel(),
                result.toString().trim(), complexity, transitions, steps.size(), info);
    }

    private void draw(String text, List<String> patterns, List<StringMatchStep> steps, int frame) {
        int n = text.length();
        boolean[] matched = new boolean[n];
        int[] state = new int[n];
        Arrays.fill(state, -1);
        for (int i = 0; i <= frame; i++) {
            StringMatchStep s = steps.get(i);
            if (s.getType() == StringMatchStep.Type.STATE_TRANSITION) state[s.getTextIndex()] = s.getPatternIndex();
            if (s.getType() == StringMatchStep.Type.FULL_MATCH) {
                int end = s.getTextIndex();
                for (String p : patterns) {
                    int start = end - p.length() + 1;
                    if (start >= 0 && text.regionMatches(start, p, 0, p.length())) {
                        for (int k = start; k <= end; k++) matched[k] = true;
                    }
                }
            }
        }
        StringMatchStep current = steps.get(frame);
        int cur = current.getTextIndex();
        ArrayVisualizer.Cell[] index = new ArrayVisualizer.Cell[n];
        ArrayVisualizer.Cell[] chars = new ArrayVisualizer.Cell[n];
        ArrayVisualizer.Cell[] states = new ArrayVisualizer.Cell[n];
        for (int i = 0; i < n; i++) {
            index[i] = ArrayVisualizer.cell(String.valueOf(i), CellState.MUTED);
            CellState st = matched[i] ? CellState.DONE : CellState.NORMAL;
            if (i == cur) st = current.getType() == StringMatchStep.Type.FULL_MATCH ? CellState.MATCH : CellState.ACTIVE;
            chars[i] = ArrayVisualizer.cell(String.valueOf(text.charAt(i)), st);
            states[i] = ArrayVisualizer.cell(state[i] < 0 ? "\u00B7" : String.valueOf(state[i]),
                    i == cur ? CellState.HIGHLIGHT : CellState.MUTED);
        }
        visualizer.render(List.of("Index", "Text", "Automaton state"), new ArrayList<>(List.of(index, chars, states)), cur);
    }
}
