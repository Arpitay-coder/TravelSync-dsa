package com.travelsync.algorithms.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Reservoir Sampling (Algorithm R, Vitter 1985): maintains a uniform random
 * sample of size k from a stream of items whose total length is unknown in
 * advance, in a single pass. Used in TravelSync to keep a representative
 * sample from a continuous booking stream for analytics without storing
 * the entire history.
 *
 * Time complexity: O(n)   Space complexity: O(k).
 */
public class ReservoirSampler<T> {

    private final int k;
    private final List<T> reservoir = new ArrayList<>();
    private final Random random;
    private long itemsSeen = 0;

    public ReservoirSampler(int k) {
        this(k, new Random());
    }

    public ReservoirSampler(int k, Random random) {
        this.k = k;
        this.random = random;
    }

    /** Feed the next item of the stream. Call once per arriving item, in order. */
    public void add(T item) {
        itemsSeen++;
        if (reservoir.size() < k) {
            reservoir.add(item);
        } else {
            long j = (long) (random.nextDouble() * itemsSeen);
            if (j < k) {
                reservoir.set((int) j, item);
            }
        }
    }

    public List<T> getSample() { return new ArrayList<>(reservoir); }

    public long getItemsSeen() { return itemsSeen; }
}
