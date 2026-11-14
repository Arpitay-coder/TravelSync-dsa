package com.travelsync.visualization;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows a pattern sliding over a text, with optional match highlighting and an LPS/Z table.
 * Used by the KMP, Z-Function and Rabin-Karp panels.
 */
public class StringMatchingVisualizer extends ArrayVisualizer {

    /**
     * Everything needed to draw one frame.
     *
     * @param alignment      text index that pattern[0] is aligned with
     * @param textIndex      text character being compared, or -1 for none
     * @param patternIndex   pattern character being compared, or -1 for none (also = matched-prefix length)
     * @param compareState   colour of the compared cells
     * @param foundMask      text positions that belong to an already reported match (may be null)
     * @param windowState    if not null, the whole window text[alignment..alignment+m) is drawn in this state
     * @param auxTable       LPS/Z table to display under the pattern, or null
     * @param auxLabel       label for the auxiliary table row (e.g. "LPS table", "Z table")
     * @param auxHighlight   index in auxTable to highlight, or -1
     */
    public record View(String text, String pattern, int alignment, int textIndex, int patternIndex,
                        CellState compareState, boolean[] foundMask, CellState windowState, int[] auxTable,
                        String auxLabel, int auxHighlight) {
    }

    public void show(View v) {
        int n = v.text().length();
        int m = v.pattern().length();
        Cell[] indexRow = new Cell[n];
        Cell[] textRow = new Cell[n];
        Cell[] patternRow = new Cell[Math.max(n, v.alignment() + m)];
        for (int i = 0; i < n; i++) {
            indexRow[i] = cell(String.valueOf(i), CellState.MUTED);
            CellState state = CellState.NORMAL;
            if (v.foundMask() != null && v.foundMask()[i]) {
                state = CellState.DONE;
            }
            if (v.windowState() != null && i >= v.alignment() && i < v.alignment() + m) {
                state = v.windowState();
            } else if (v.patternIndex() > 0 && i >= v.alignment() && i < v.alignment() + v.patternIndex()) {
                state = CellState.DONE;
            }
            if (i == v.textIndex()) {
                state = v.compareState();
            }
            textRow[i] = cell(String.valueOf(v.text().charAt(i)), state);
        }
        for (int k = 0; k < m; k++) {
            CellState state = CellState.NORMAL;
            if (v.windowState() != null) {
                state = v.windowState();
            } else if (k < v.patternIndex()) {
                state = CellState.DONE;
            }
            if (k == v.patternIndex() && v.textIndex() >= 0) {
                state = v.compareState();
            }
            patternRow[v.alignment() + k] = cell(String.valueOf(v.pattern().charAt(k)), state);
        }
        List<String> names = new ArrayList<>(List.of("Index", "Text", "Pattern"));
        List<Cell[]> rows = new ArrayList<>(List.of(indexRow, textRow, patternRow));
        if (v.auxTable() != null && m > 0) {
            Cell[] patternCopy = new Cell[m];
            Cell[] auxRow = new Cell[m];
            for (int k = 0; k < m; k++) {
                patternCopy[k] = cell(String.valueOf(v.pattern().charAt(k)), CellState.NORMAL);
                auxRow[k] = cell(String.valueOf(v.auxTable()[k]), k == v.auxHighlight() ? CellState.HIGHLIGHT : CellState.MUTED);
            }
            names.add("Pattern");
            rows.add(patternCopy);
            names.add(v.auxLabel() == null ? "Aux table" : v.auxLabel());
            rows.add(auxRow);
        }
        render(names, rows, v.textIndex() >= 0 ? v.textIndex() : v.alignment());
    }
}
