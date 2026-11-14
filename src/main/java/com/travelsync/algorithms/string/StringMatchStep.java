package com.travelsync.algorithms.string;

/**
 * A single recorded step of a string-matching algorithm's execution, used to
 * drive the UI's Play/Pause/Step/Reset visualizations. Each step is a real
 * snapshot produced while the algorithm actually ran -- never fabricated.
 */
public class StringMatchStep {

    public enum Type { COMPARE, MATCH, MISMATCH, SHIFT, FULL_MATCH, TABLE_BUILD, STATE_TRANSITION, INFO }

    private final Type type;
    private final int textIndex;      // index into text being compared, -1 if n/a
    private final int patternIndex;   // index into pattern being compared, -1 if n/a
    private final String label;       // human-readable explanation of this step
    private final int[] auxiliaryArraySnapshot; // e.g. LPS/Z/failure array state at this point

    public StringMatchStep(Type type, int textIndex, int patternIndex, String label,
                            int[] auxiliaryArraySnapshot) {
        this.type = type;
        this.textIndex = textIndex;
        this.patternIndex = patternIndex;
        this.label = label;
        this.auxiliaryArraySnapshot = auxiliaryArraySnapshot;
    }

    public StringMatchStep(Type type, int textIndex, int patternIndex, String label) {
        this(type, textIndex, patternIndex, label, null);
    }

    public Type getType() { return type; }
    public int getTextIndex() { return textIndex; }
    public int getPatternIndex() { return patternIndex; }
    public String getLabel() { return label; }
    public int[] getAuxiliaryArraySnapshot() { return auxiliaryArraySnapshot; }

    @Override
    public String toString() {
        return "[" + type + "] textIdx=" + textIndex + " patIdx=" + patternIndex + " :: " + label;
    }
}
