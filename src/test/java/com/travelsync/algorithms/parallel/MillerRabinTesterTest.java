package com.travelsync.algorithms.parallel;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class MillerRabinTesterTest {

    @Test
    void identifiesKnownSmallPrimes() {
        int[] primes = {2, 3, 5, 7, 11, 13, 97, 101};
        for (int p : primes) {
            assertTrue(MillerRabinTester.isProbablyPrime(p, 20), p + " should be prime");
        }
    }

    @Test
    void identifiesKnownComposites() {
        int[] composites = {4, 6, 8, 9, 15, 21, 100, 121};
        for (int c : composites) {
            assertFalse(MillerRabinTester.isProbablyPrime(c, 20), c + " should be composite");
        }
    }

    @Test
    void handlesLargeKnownPrime() {
        // A known large prime (2^61 - 1, a Mersenne prime)
        BigInteger mersenne61 = BigInteger.valueOf(2).pow(61).subtract(BigInteger.ONE);
        assertTrue(MillerRabinTester.isProbablyPrime(mersenne61, 25));
    }

    @Test
    void handlesLargeKnownComposite() {
        BigInteger n = BigInteger.valueOf(2).pow(61).subtract(BigInteger.ONE).multiply(BigInteger.valueOf(3));
        assertFalse(MillerRabinTester.isProbablyPrime(n, 25));
    }

    @Test
    void rejectsOneAndBelow() {
        assertFalse(MillerRabinTester.isProbablyPrime(1, 10));
        assertFalse(MillerRabinTester.isProbablyPrime(0, 10));
    }
}
