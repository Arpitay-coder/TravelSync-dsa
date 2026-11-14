package com.travelsync.algorithms.parallel;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RandomizedQuickSortTest {

    @Test
    void sortsRandomIntegers() {
        List<Integer> list = new ArrayList<>(List.of(5, 3, 8, 1, 9, 2, 7));
        RandomizedQuickSort.sort(list, Integer::compareTo, new Random(42));
        assertEquals(List.of(1, 2, 3, 5, 7, 8, 9), list);
    }

    @Test
    void sortsAlreadySortedInput() {
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        RandomizedQuickSort.sort(list, Integer::compareTo, new Random(1));
        assertEquals(List.of(1, 2, 3, 4, 5), list);
    }

    @Test
    void sortsReverseSortedInputWithoutWorstCaseFailure() {
        List<Integer> list = new ArrayList<>();
        for (int i = 1000; i > 0; i--) list.add(i);
        RandomizedQuickSort.sort(list, Integer::compareTo, new Random(7));
        for (int i = 0; i < list.size(); i++) assertEquals(i + 1, list.get(i));
    }

    @Test
    void handlesEmptyAndSingleElementLists() {
        List<Integer> empty = new ArrayList<>();
        RandomizedQuickSort.sort(empty, Integer::compareTo);
        assertTrue(empty.isEmpty());

        List<Integer> single = new ArrayList<>(List.of(42));
        RandomizedQuickSort.sort(single, Integer::compareTo);
        assertEquals(List.of(42), single);
    }

    @Test
    void sortsByDescendingComparatorForPreferenceRanking() {
        List<Integer> scores = new ArrayList<>(List.of(70, 95, 60, 88));
        RandomizedQuickSort.sort(scores, Comparator.reverseOrder(), new Random(3));
        assertEquals(List.of(95, 88, 70, 60), scores);
    }
}
