package com.travelsync.ui.panel.parallel;

import com.travelsync.algorithms.parallel.MillerRabinTester;
import com.travelsync.ui.AlgorithmPanel;
import com.travelsync.ui.Execution;
import com.travelsync.ui.Ui;
import com.travelsync.util.ValidationException;
import com.travelsync.visualization.CellState;
import com.travelsync.visualization.RowTableVisualizer;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

/** Miller-Rabin panel: tests large numeric identifiers (e.g. booking checksums) for primality, witness by witness. */
public final class MillerRabinPanel extends AlgorithmPanel {

    private final TextField numberField = new TextField();
    private final Spinner<Integer> roundsSpinner = new Spinner<>(1, 40, 12);
    private final RowTableVisualizer table = new RowTableVisualizer("Round", "Random base a", "a^d, a^2d, ... mod n", "Verdict");

    public MillerRabinPanel() {
        roundsSpinner.setEditable(true);
        roundsSpinner.setPrefWidth(80);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.add(Ui.label("Number n", "kv-key"), 0, 0);
        grid.add(numberField, 1, 0);
        grid.add(Ui.label("Rounds", "kv-key"), 2, 0);
        grid.add(roundsSpinner, 3, 0);
        GridPane.setHgrow(numberField, Priority.ALWAYS);
        assemble("Miller-Rabin primality test",
                "Travel problem: large numeric identifiers (booking checksums, capacity-planning numbers) sometimes need a fast primality check. "
                        + "n \u2212 1 = 2\u02B3\u00B7d; for a random base a, if a\u1d48 mod n is 1 or \u22121, or squaring it ever reaches \u22121, a is not a witness "
                        + "against n. One witness proves n composite; surviving all rounds makes n prime with error probability at most 4\u207B\u1D4F.",
                grid, table);
        loadDemo();
    }

    @Override
    protected void loadDemo() {
        numberField.setText("97");
        roundsSpinner.getValueFactory().setValue(12);
    }

    @Override
    protected Supplier<Execution> prepare() throws ValidationException {
        BigInteger n;
        try {
            n = new BigInteger(numberField.getText().trim());
        } catch (NumberFormatException | NullPointerException ex) {
            throw new ValidationException("Please enter a valid non-negative integer.");
        }
        if (n.bitLength() > 200) throw new ValidationException("Please use a number with at most ~60 digits so the table stays readable.");
        int rounds;
        try {
            rounds = roundsSpinner.getValue();
        } catch (NullPointerException ex) {
            throw new ValidationException("Please enter a whole number of rounds.");
        }
        return () -> execute(n, rounds);
    }

    private Execution execute(BigInteger n, int rounds) {
        MillerRabinTester.Trace trace = MillerRabinTester.trace(n, rounds, new Random());
        int frames = trace.rounds().isEmpty() ? 1 : trace.rounds().size();
        String result = trace.earlyReason() != null ? trace.earlyReason()
                : n + " is " + (trace.probablyPrime() ? "PROBABLY PRIME" : "COMPOSITE") + " after " + trace.rounds().size() + " round(s).";
        String complexity = "Time O(k \u00B7 log\u00B3 n) for k rounds   |   Space O(log n)";
        String info = trace.decomposed() ? "n \u2212 1 = 2^" + trace.r() + " \u00B7 " + trace.d() + "." : "Decided without witnesses.";
        return new Execution(frames, f -> draw(trace, f), f -> narrate(trace, f), result, complexity, -1, frames, info);
    }

    private void draw(MillerRabinTester.Trace trace, int frame) {
        List<String[]> rows = new ArrayList<>();
        Map<Integer, CellState> states = new HashMap<>();
        if (trace.rounds().isEmpty()) {
            table.show(rows, states);
            return;
        }
        for (int i = 0; i <= frame; i++) {
            MillerRabinTester.Round r = trace.rounds().get(i);
            String seq = r.sequence().size() > 5 ? r.sequence().subList(0, 5) + "\u2026" : r.sequence().toString();
            rows.add(new String[]{String.valueOf(i + 1), r.base().toString(), seq, r.provesComposite() ? "witness \u2013 composite" : "no evidence"});
            states.put(i, i == frame ? (r.provesComposite() ? CellState.MISMATCH : CellState.ACTIVE) : (r.provesComposite() ? CellState.MISMATCH : CellState.NORMAL));
        }
        table.show(rows, states);
    }

    private static String narrate(MillerRabinTester.Trace trace, int frame) {
        if (trace.rounds().isEmpty()) return trace.earlyReason();
        MillerRabinTester.Round r = trace.rounds().get(frame);
        String text = "Round " + (frame + 1) + ", base a = " + r.base() + ": " + r.reason();
        if (r.provesComposite()) return text + " The search stops here.";
        if (frame == trace.rounds().size() - 1) return text + " All rounds passed, so n is reported probably prime.";
        return text;
    }
}
