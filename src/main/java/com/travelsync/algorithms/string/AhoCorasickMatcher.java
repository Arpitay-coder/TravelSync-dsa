package com.travelsync.algorithms.string;

import java.util.*;

/**
 * Aho-Corasick multi-pattern matching automaton.
 * Used in TravelSync to detect multiple destination/service keywords in a single pass
 * over free-text (e.g. a travel document or a traveler's search query).
 *
 * Build time: O(sum of pattern lengths).   Search time: O(n + number of matches).
 */
public final class AhoCorasickMatcher {

    private static class Node {
        Map<Character, Integer> children = new HashMap<>();
        int fail = 0;
        List<String> output = new ArrayList<>();
    }

    private final List<Node> trie = new ArrayList<>();

    public AhoCorasickMatcher(Collection<String> patterns) {
        trie.add(new Node()); // root
        for (String p : patterns) {
            if (!p.isEmpty()) addPattern(p);
        }
        buildFailureLinks();
    }

    private void addPattern(String pattern) {
        int node = 0;
        for (char c : pattern.toCharArray()) {
            node = trie.get(node).children.computeIfAbsent(c, k -> {
                trie.add(new Node());
                return trie.size() - 1;
            });
        }
        trie.get(node).output.add(pattern);
    }

    private void buildFailureLinks() {
        Deque<Integer> queue = new ArrayDeque<>();
        for (Map.Entry<Character, Integer> e : trie.get(0).children.entrySet()) {
            trie.get(e.getValue()).fail = 0;
            queue.add(e.getValue());
        }
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (Map.Entry<Character, Integer> e : trie.get(u).children.entrySet()) {
                char c = e.getKey();
                int v = e.getValue();
                int f = trie.get(u).fail;
                while (f != 0 && !trie.get(f).children.containsKey(c)) {
                    f = trie.get(f).fail;
                }
                if (trie.get(f).children.containsKey(c) && trie.get(f).children.get(c) != v) {
                    f = trie.get(f).children.get(c);
                }
                trie.get(v).fail = f;
                trie.get(v).output.addAll(trie.get(f).output);
                queue.add(v);
            }
        }
    }

    /** Returns map of pattern -> list of end indices (inclusive) where it was found in text. */
    public Map<String, List<Integer>> search(String text) {
        Map<String, List<Integer>> results = new LinkedHashMap<>();
        int state = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            while (state != 0 && !trie.get(state).children.containsKey(c)) {
                state = trie.get(state).fail;
            }
            if (trie.get(state).children.containsKey(c)) {
                state = trie.get(state).children.get(c);
            }
            for (String matched : trie.get(state).output) {
                results.computeIfAbsent(matched, k -> new ArrayList<>()).add(i);
            }
        }
        return results;
    }

    /** Full state-transition trace for the automaton visualizer. */
    public List<StringMatchStep> trace(String text) {
        List<StringMatchStep> steps = new ArrayList<>();
        int state = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int before = state;
            while (state != 0 && !trie.get(state).children.containsKey(c)) {
                state = trie.get(state).fail;
                steps.add(new StringMatchStep(StringMatchStep.Type.STATE_TRANSITION, i, state,
                        "Follow failure link from state " + before + " to " + state));
                before = state;
            }
            if (trie.get(state).children.containsKey(c)) {
                state = trie.get(state).children.get(c);
            }
            steps.add(new StringMatchStep(StringMatchStep.Type.STATE_TRANSITION, i, state,
                    "Read '" + c + "' -> state " + state));
            for (String matched : trie.get(state).output) {
                steps.add(new StringMatchStep(StringMatchStep.Type.FULL_MATCH, i, state,
                        "Pattern \"" + matched + "\" matched, ending at index " + i));
            }
        }
        return steps;
    }

    public int getStateCount() { return trie.size(); }
}
