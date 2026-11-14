package com.travelsync.algorithms.parallel;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ReservoirSamplerTest {

    @Test
    void sampleNeverExceedsK() {
        ReservoirSampler<Integer> sampler = new ReservoirSampler<>(5, new Random(1));
        for (int i = 0; i < 1000; i++) sampler.add(i);
        assertEquals(5, sampler.getSample().size());
    }

    @Test
    void sampleEqualsStreamWhenStreamSmallerThanK() {
        ReservoirSampler<Integer> sampler = new ReservoirSampler<>(10, new Random(1));
        for (int i = 0; i < 4; i++) sampler.add(i);
        assertEquals(4, sampler.getSample().size());
    }

    @Test
    void tracksTotalItemsSeen() {
        ReservoirSampler<String> sampler = new ReservoirSampler<>(3);
        for (int i = 0; i < 50; i++) sampler.add("booking-" + i);
        assertEquals(50, sampler.getItemsSeen());
    }

    @Test
    void allSampledItemsCameFromTheStream() {
        ReservoirSampler<Integer> sampler = new ReservoirSampler<>(5, new Random(2));
        for (int i = 0; i < 200; i++) sampler.add(i);
        for (int v : sampler.getSample()) {
            assertTrue(v >= 0 && v < 200);
        }
    }
}
