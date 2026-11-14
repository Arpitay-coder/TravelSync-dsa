package com.travelsync.ui.panel.string;

import com.travelsync.algorithms.string.KMPMatcher;
import com.travelsync.algorithms.string.StringMatchStep;
import com.travelsync.data.AppDataStore;
import com.travelsync.model.Destination;
import com.travelsync.ui.AlgorithmPanel;
import com.travelsync.ui.Execution;
import com.travelsync.ui.Ui;
import com.travelsync.util.ValidationException;
import com.travelsync.visualization.CellState;
import com.travelsync.visualization.StringMatchingVisualizer;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** KMP panel: searches destination / hotel names and travel keywords, animating comparisons, LPS table and fallbacks. */
public final class KmpPanel extends AlgorithmPanel {

    private final TextField textField = new TextField();
    private final TextField patternField = new TextField();
    private final StringMatchingVisualizer visualizer = new StringMatchingVisualizer();

    public KmpPanel() {
        textField.setPromptText("Text to search, e.g. a list of destination names or a travel description");
        patternField.setPromptText("Pattern, e.g. Santorini");
        Button useData = Ui.button("Use destination names from data", "btn-secondary",
                "Fill the text with the destination names of the current data set");
        useData.setOnAction(e -> textField.setText(AppDataStore.getInstance().getDestinations().stream()
                .map(Destination::getName).collect(Collectors.joining(" "))));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(Ui.label("Text", "kv-key"), 0, 0);
        grid.add(textField, 1, 0);
        grid.add(useData, 2, 0);
        grid.add(Ui.label("Pattern", "kv-key"), 0, 1);
        grid.add(patternField, 1, 1);
        GridPane.setHgrow(textField, Priority.ALWAYS);
        assemble("KMP \u2013 Knuth-Morris-Pratt",
                "Travel problem: find a destination, hotel name or travel keyword inside a large catalogue without re-reading text. "
                        + "KMP precomputes the LPS table so that after a mismatch the pattern pointer falls back inside the pattern "
                        + "and the text pointer never moves backwards.",
                grid, visualizer);
        loadDemo();
    }

    @Override
    protected void loadDemo() {
        textField.setText("Kyoto Santorini Banff Santorini Marrakech SantoriniBay Bali");
        patternField.setText("Santorini");
    }

    @Override
    protected Supplier<Execution> prepare() throws ValidationException {
        String text = StringInputs.requireText(textField.getText(), "text");
        String pattern = StringInputs.requireText(patternField.getText(), "pattern");
        if (pattern.length() > text.length()) {
            throw new ValidationException("The pattern is longer than the text, so it cannot occur in it.");
        }
        return () -> execute(text, pattern);
    }

    private Execution execute(String text, String pattern) {
        List<StringMatchStep> steps = KMPMatcher.trace(text, pattern);
        int[] lps = KMPMatcher.buildLpsTable(pattern);
        List<Integer> matches = KMPMatcher.search(text, pattern);
        long comparisons = steps.stream().filter(s -> s.getType() == StringMatchStep.Type.COMPARE
                || s.getType() == StringMatchStep.Type.MISMATCH).count();
        int n = text.length();
        int m = pattern.length();
        String result = (matches.isEmpty() ? "No occurrence found." : matches.size() + " occurrence(s) at index " + matches)
                + "\nLPS = " + Arrays.toString(lps);
        String complexity = "Time O(n + m) = O(" + n + " + " + m + ")   |   Space O(m) for the LPS table";
        String info = "Naive matching may need up to n\u00B7m = " + (long) n * m + " comparisons; KMP used " + comparisons
                + " (never more than 2n = " + 2L * n + ").";
        return new Execution(steps.size(), f -> visualizer.show(view(text, pattern, lps, steps, f)),
                f -> steps.get(f).getLabel(), result, complexity, comparisons, steps.size(), info);
    }

    private static StringMatchingVisualizer.View view(String text, String pattern, int[] lps,
                                                      List<StringMatchStep> steps, int frame) {
        int m = pattern.length();
        StringMatchStep s = steps.get(frame);
        boolean[] found = new boolean[text.length()];
        for (int i = 0; i <= frame; i++) {
            StringMatchStep earlier = steps.get(i);
            if (earlier.getType() == StringMatchStep.Type.FULL_MATCH) {
                for (int k = 0; k < m; k++) found[earlier.getTextIndex() + k] = true;
            }
        }
        return switch (s.getType()) {
            case TABLE_BUILD -> new StringMatchingVisualizer.View(text, pattern, 0, -1, -1, CellState.NORMAL, null, null,
                    s.getAuxiliaryArraySnapshot(), "LPS table", s.getPatternIndex());
            case COMPARE -> new StringMatchingVisualizer.View(text, pattern, s.getTextIndex() - s.getPatternIndex(),
                    s.getTextIndex(), s.getPatternIndex(), CellState.MATCH, found, null, lps, "LPS table", -1);
            case MISMATCH -> new StringMatchingVisualizer.View(text, pattern, s.getTextIndex() - s.getPatternIndex(),
                    s.getTextIndex(), s.getPatternIndex(), CellState.MISMATCH, found, null, lps, "LPS table",
                    s.getPatternIndex() > 0 ? s.getPatternIndex() - 1 : -1);
            case FULL_MATCH -> new StringMatchingVisualizer.View(text, pattern, s.getTextIndex(), -1, -1,
                    CellState.MATCH, found, CellState.MATCH, lps, "LPS table", m - 1);
            default -> new StringMatchingVisualizer.View(text, pattern, 0, -1, -1, CellState.NORMAL, found, null,
                    s.getLabel().startsWith("Starting") ? lps : null, "LPS table", -1);
        };
    }
}
