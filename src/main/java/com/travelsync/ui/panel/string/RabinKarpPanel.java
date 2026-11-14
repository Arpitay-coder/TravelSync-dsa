package com.travelsync.ui.panel.string;

import com.travelsync.algorithms.string.RabinKarpMatcher;
import com.travelsync.algorithms.string.StringMatchStep;
import com.travelsync.data.AppDataStore;
import com.travelsync.model.Booking;
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

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Rabin-Karp panel: searches booking / reservation codes with a rolling hash, verifying every hash hit. */
public final class RabinKarpPanel extends AlgorithmPanel {

    private final TextField textField = new TextField();
    private final TextField patternField = new TextField();
    private final StringMatchingVisualizer visualizer = new StringMatchingVisualizer();

    public RabinKarpPanel() {
        Button useData = Ui.button("Use booking codes from data", "btn-secondary",
                "Fill the text with the first booking codes of the current data set");
        useData.setOnAction(e -> {
            List<Booking> bookings = AppDataStore.getInstance().getBookings();
            textField.setText(bookings.stream().limit(6).map(Booking::getId).collect(Collectors.joining(" ")));
            if (!bookings.isEmpty()) patternField.setText(bookings.get(0).getId());
        });
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(Ui.label("Text", "kv-key"), 0, 0);
        grid.add(textField, 1, 0);
        grid.add(useData, 2, 0);
        grid.add(Ui.label("Booking code", "kv-key"), 0, 1);
        grid.add(patternField, 1, 1);
        GridPane.setHgrow(textField, Priority.ALWAYS);
        assemble("Rabin-Karp",
                "Travel problem: locate a booking or reservation code in a long log of records. A rolling polynomial hash "
                        + "of the sliding window is compared with the code's hash in O(1); only equal hashes are verified "
                        + "character by character, which also exposes hash collisions.",
                grid, visualizer);
        loadDemo();
    }

    @Override
    protected void loadDemo() {
        textField.setText("BK-7F2A19 BK-3C90AB BK-7F2A19-X BK-11D4E2 BK-7F2A19");
        patternField.setText("BK-7F2A19");
    }

    @Override
    protected Supplier<Execution> prepare() throws ValidationException {
        String text = StringInputs.requireText(textField.getText(), "text");
        String pattern = StringInputs.requireText(patternField.getText(), "booking code");
        if (pattern.length() > text.length()) {
            throw new ValidationException("The code is longer than the text, so it cannot occur in it.");
        }
        return () -> execute(text, pattern);
    }

    private Execution execute(String text, String pattern) {
        List<StringMatchStep> steps = RabinKarpMatcher.trace(text, pattern);
        List<Integer> matches = RabinKarpMatcher.search(text, pattern);
        long windows = steps.stream().filter(s -> s.getType() == StringMatchStep.Type.COMPARE).count();
        long collisions = steps.stream().filter(s -> s.getType() == StringMatchStep.Type.MISMATCH).count();
        int n = text.length();
        int m = pattern.length();
        String result = matches.isEmpty() ? "No occurrence found." : matches.size() + " occurrence(s) at index " + matches;
        String complexity = "Expected O(n + m) = O(" + n + " + " + m + ")   |   Worst case O(n\u00B7m)   |   Space O(1)";
        String info = windows + " window hash(es) computed, " + collisions + " hash collision(s) detected and rejected.";
        return new Execution(steps.size(), f -> visualizer.show(view(text, pattern, steps, f)),
                f -> steps.get(f).getLabel(), result, complexity, windows, steps.size(), info);
    }

    private static StringMatchingVisualizer.View view(String text, String pattern, List<StringMatchStep> steps, int frame) {
        int m = pattern.length();
        StringMatchStep s = steps.get(frame);
        boolean[] found = new boolean[text.length()];
        for (int i = 0; i <= frame; i++) {
            StringMatchStep e = steps.get(i);
            if (e.getType() == StringMatchStep.Type.FULL_MATCH) {
                for (int k = 0; k < m; k++) found[e.getTextIndex() + k] = true;
            }
        }
        CellState window = switch (s.getType()) {
            case FULL_MATCH -> CellState.MATCH;
            case MISMATCH -> CellState.MISMATCH;
            case COMPARE -> CellState.WINDOW;
            default -> null;
        };
        int alignment = s.getType() == StringMatchStep.Type.INFO ? 0 : s.getTextIndex();
        return new StringMatchingVisualizer.View(text, pattern, alignment, -1, -1, CellState.NORMAL, found, window,
                null, null, -1);
    }
}
