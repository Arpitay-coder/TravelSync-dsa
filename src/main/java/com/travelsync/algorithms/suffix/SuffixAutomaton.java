package com.travelsync.algorithms.suffix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Suffix Automaton (Blumer et al.), built online in O(n) amortized time.
 * Used in TravelSync as the substring-search / document-similarity structure
 * satisfying the "Suffix Trees / Suffix Automaton" requirement of M2 --
 * every substring of the source string corresponds to a walk from the
 * initial state, making membership and longest-common-substring queries
 * fast without building a full suffix tree.
 *
 * Construction time: O(n)   Space: O(n) states (at most 2n-1).
 * Substring query: O(m) where m = query length.
 */
public class SuffixAutomaton {

    private static class State {
        int len;
        int link;
        Map<Character, Integer> next = new HashMap<>();
    }

    private final List<State> states = new ArrayList<>();
    private int last;

    public SuffixAutomaton(String s) {
        State initial = new State();
        initial.len = 0;
        initial.link = -1;
        states.add(initial);
        last = 0;
        for (char c : s.toCharArray()) {
            extend(c);
        }
    }

    private void extend(char c) {
        int cur = states.size();
        State curState = new State();
        curState.len = states.get(last).len + 1;
        states.add(curState);

        int p = last;
        while (p != -1 && !states.get(p).next.containsKey(c)) {
            states.get(p).next.put(c, cur);
            p = states.get(p).link;
        }

        if (p == -1) {
            curState.link = 0;
        } else {
            int q = states.get(p).next.get(c);
            if (states.get(p).len + 1 == states.get(q).len) {
                curState.link = q;
            } else {
                int clone = states.size();
                State cloneState = new State();
                cloneState.len = states.get(p).len + 1;
                cloneState.next = new HashMap<>(states.get(q).next);
                cloneState.link = states.get(q).link;
                states.add(cloneState);

                while (p != -1 && states.get(p).next.get(c) != null && states.get(p).next.get(c) == q) {
                    states.get(p).next.put(c, clone);
                    p = states.get(p).link;
                }
                states.get(q).link = clone;
                curState.link = clone;
            }
        }
        last = cur;
    }

    /** True if p occurs as a substring anywhere in the source string. */
    public boolean containsSubstring(String p) {
        int state = 0;
        for (char c : p.toCharArray()) {
            Integer next = states.get(state).next.get(c);
            if (next == null) return false;
            state = next;
        }
        return true;
    }

    /**
     * Finds the longest substring common to this automaton's source string and `other`,
     * by walking `other` against the automaton and falling back through suffix links
     * whenever a transition is missing (the classic LCS-via-suffix-automaton technique).
     */
    public String longestCommonSubstring(String other) {
        int state = 0, length = 0;
        int bestLength = 0, bestEnd = 0;
        for (int i = 0; i < other.length(); i++) {
            char c = other.charAt(i);
            while (state != -1 && !states.get(state).next.containsKey(c)) {
                state = states.get(state).link;
                length = state == -1 ? 0 : states.get(state).len;
            }
            if (state == -1) {
                state = 0;
                length = 0;
            } else {
                state = states.get(state).next.get(c);
                length++;
            }
            if (length > bestLength) {
                bestLength = length;
                bestEnd = i;
            }
        }
        return bestLength == 0 ? "" : other.substring(bestEnd - bestLength + 1, bestEnd + 1);
    }

    /** One step of walking a text through the automaton: the current common-substring length and state. */
    public record WalkStep(int index, int length, int state, boolean fellBack) { }

    /** Records the same walk that {@link #longestCommonSubstring(String)} performs, step by step. */
    public List<WalkStep> walk(String other) {
        List<WalkStep> steps = new ArrayList<>();
        int state = 0, length = 0;
        for (int i = 0; i < other.length(); i++) {
            char c = other.charAt(i);
            boolean fellBack = false;
            while (state != -1 && !states.get(state).next.containsKey(c)) {
                state = states.get(state).link;
                length = state == -1 ? 0 : states.get(state).len;
                fellBack = true;
            }
            if (state == -1) {
                state = 0;
                length = 0;
            } else {
                state = states.get(state).next.get(c);
                length++;
            }
            steps.add(new WalkStep(i, length, state, fellBack));
        }
        return steps;
    }

    public int getStateCount() { return states.size(); }
}
