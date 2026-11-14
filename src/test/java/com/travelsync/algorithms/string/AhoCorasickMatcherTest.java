package com.travelsync.algorithms.string;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AhoCorasickMatcherTest {

    @Test
    void detectsMultipleKeywordsInSinglePass() {
        AhoCorasickMatcher matcher = new AhoCorasickMatcher(List.of("beach", "resort", "spa"));
        Map<String, List<Integer>> results = matcher.search("luxury beach resort with spa access");
        assertEquals(Set.of("beach", "resort", "spa"), results.keySet());
    }

    @Test
    void detectsOverlappingPatterns() {
        AhoCorasickMatcher matcher = new AhoCorasickMatcher(List.of("he", "she", "his", "hers"));
        Map<String, List<Integer>> results = matcher.search("ushers");
        assertTrue(results.containsKey("she"));
        assertTrue(results.containsKey("he"));
        assertTrue(results.containsKey("hers"));
    }

    @Test
    void returnsEmptyMapWhenNoKeywordsPresent() {
        AhoCorasickMatcher matcher = new AhoCorasickMatcher(List.of("volcano", "glacier"));
        assertTrue(matcher.search("sunny tropical island").isEmpty());
    }

    @Test
    void handlesEmptyPatternListGracefully() {
        AhoCorasickMatcher matcher = new AhoCorasickMatcher(List.of());
        assertTrue(matcher.search("any text here").isEmpty());
    }

    @Test
    void traceProducesStateTransitionsForEveryCharacter() {
        AhoCorasickMatcher matcher = new AhoCorasickMatcher(List.of("spa"));
        List<StringMatchStep> steps = matcher.trace("day spa retreat");
        assertFalse(steps.isEmpty());
        assertTrue(steps.stream().anyMatch(s -> s.getType() == StringMatchStep.Type.FULL_MATCH));
    }
}
