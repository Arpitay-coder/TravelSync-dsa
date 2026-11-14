package com.travelsync.algorithms.dp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatrixChainOptimizerTest {

    @Test
    void computesKnownMinimumCost() {
        // Classic textbook example: dims 40x20, 20x30, 30x10, 10x30
        int[] dims = {40, 20, 30, 10, 30};
        var result = MatrixChainOptimizer.compute(dims);
        assertEquals(26000, result.minMultiplications());
    }

    @Test
    void singleMatrixNeedsNoMultiplication() {
        int[] dims = {10, 20};
        var result = MatrixChainOptimizer.compute(dims);
        assertEquals(0, result.minMultiplications());
        assertEquals("M1", result.parenthesization());
    }

    @Test
    void twoMatricesHaveOnlyOneParenthesization() {
        int[] dims = {10, 20, 30};
        var result = MatrixChainOptimizer.compute(dims);
        assertEquals(10L * 20 * 30, result.minMultiplications());
        assertEquals("(M1 x M2)", result.parenthesization());
    }

    @Test
    void parenthesizationCoversAllMatrices() {
        int[] dims = {5, 10, 3, 12, 5, 50, 6};
        var result = MatrixChainOptimizer.compute(dims);
        for (int i = 1; i <= dims.length - 1; i++) {
            assertTrue(result.parenthesization().contains("M" + i));
        }
    }

    @Test
    void costTableHoldsMinimumInTopRightCell() {
        int[] dims = {40, 20, 30, 10, 30};
        var result = MatrixChainOptimizer.compute(dims);
        assertEquals(result.minMultiplications(), result.costTable()[1][4]);
        assertEquals(24000, result.costTable()[1][2]); // 40*20*30
        assertEquals(6000, result.costTable()[2][3]);  // 20*30*10
    }
}
