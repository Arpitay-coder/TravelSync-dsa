package com.travelsync.algorithms.parallel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Miller-Rabin probabilistic primality test. Used in TravelSync wherever
 * large numerical identifiers (booking reference checksums, large capacity
 * planning numbers) need fast primality checks without full factorization.
 *
 * Time complexity: O(k log^3 n) for k rounds on an n-bit number.
 * Error probability: at most 4^-k that a composite is misreported as prime.
 */
public final class MillerRabinTester {

    private MillerRabinTester() { }

    public static boolean isProbablyPrime(BigInteger n, int rounds) {
        if (n.compareTo(BigInteger.TWO) < 0) return false;
        if (n.equals(BigInteger.TWO) || n.equals(BigInteger.valueOf(3))) return true;
        if (n.testBit(0) == false) return false; // even

        BigInteger nMinusOne = n.subtract(BigInteger.ONE);
        BigInteger d = nMinusOne;
        int r = 0;
        while (d.testBit(0) == false) {
            d = d.shiftRight(1);
            r++;
        }

        Random random = new Random();
        for (int i = 0; i < rounds; i++) {
            BigInteger a = randomInRange(BigInteger.TWO, n.subtract(BigInteger.TWO), random);
            BigInteger x = a.modPow(d, n);
            if (x.equals(BigInteger.ONE) || x.equals(nMinusOne)) continue;

            boolean composite = true;
            for (int j = 0; j < r - 1; j++) {
                x = x.modPow(BigInteger.TWO, n);
                if (x.equals(nMinusOne)) { composite = false; break; }
            }
            if (composite) return false;
        }
        return true;
    }

    public static boolean isProbablyPrime(long n, int rounds) {
        return isProbablyPrime(BigInteger.valueOf(n), rounds);
    }

    private static BigInteger randomInRange(BigInteger min, BigInteger max, Random random) {
        BigInteger range = max.subtract(min).add(BigInteger.ONE);
        BigInteger result;
        do {
            result = new BigInteger(range.bitLength(), random);
        } while (result.compareTo(range) >= 0);
        return result.add(min);
    }

    /** One witness round: the base, the sequence a^d, a^(2d), ... mod n, and whether it proves n composite. */
    public record Round(BigInteger base, List<BigInteger> sequence, boolean provesComposite, String reason) { }

    /**
     * Full trace of a test.
     *
     * @param decomposed  n - 1 = 2^r * d was computed (false for the trivial early verdicts)
     * @param earlyReason explanation for numbers decided without witnesses (small, even), else null
     */
    public record Trace(BigInteger n, BigInteger d, int r, boolean decomposed, String earlyReason, List<Round> rounds,
                        boolean probablyPrime) { }

    /** Runs the test round by round and records every witness sequence; uses the supplied random source. */
    public static Trace trace(BigInteger n, int rounds, Random random) {
        if (n.compareTo(BigInteger.TWO) < 0) {
            return new Trace(n, BigInteger.ZERO, 0, false, n + " is smaller than 2, so it is not prime.", List.of(), false);
        }
        if (n.equals(BigInteger.TWO) || n.equals(BigInteger.valueOf(3))) {
            return new Trace(n, BigInteger.ZERO, 0, false, n + " is 2 or 3, the smallest primes.", List.of(), true);
        }
        if (!n.testBit(0)) {
            return new Trace(n, BigInteger.ZERO, 0, false, n + " is even and greater than 2, so it is composite.", List.of(), false);
        }
        BigInteger nMinusOne = n.subtract(BigInteger.ONE);
        BigInteger d = nMinusOne;
        int r = 0;
        while (!d.testBit(0)) {
            d = d.shiftRight(1);
            r++;
        }
        List<Round> list = new ArrayList<>();
        for (int i = 0; i < rounds; i++) {
            BigInteger a = randomInRange(BigInteger.TWO, n.subtract(BigInteger.TWO), random);
            List<BigInteger> sequence = new ArrayList<>();
            BigInteger x = a.modPow(d, n);
            sequence.add(x);
            boolean passes = x.equals(BigInteger.ONE) || x.equals(nMinusOne);
            String reason = passes ? (x.equals(BigInteger.ONE) ? "a^d \u2261 1: no evidence against n." : "a^d \u2261 \u22121: no evidence against n.") : null;
            for (int j = 0; j < r - 1 && !passes; j++) {
                x = x.modPow(BigInteger.TWO, n);
                sequence.add(x);
                if (x.equals(nMinusOne)) {
                    passes = true;
                    reason = "reached \u22121 after " + (j + 1) + " squaring(s): no evidence against n.";
                }
            }
            if (!passes) reason = "the sequence never reaches \u22121 (and did not start at 1): a is a witness, n is composite.";
            list.add(new Round(a, sequence, !passes, reason));
            if (!passes) return new Trace(n, d, r, true, null, list, false);
        }
        return new Trace(n, d, r, true, null, list, true);
    }
}
