package com.travelsync.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoTest {

    @Test
    void distanceToSelfIsZero() {
        assertEquals(0.0, Geo.haversineKm(35.0116, 135.7681, 35.0116, 135.7681), 1e-9);
    }

    @Test
    void distanceIsSymmetric() {
        double ab = Geo.haversineKm(35.0116, 135.7681, 36.3932, 25.4615);
        double ba = Geo.haversineKm(36.3932, 25.4615, 35.0116, 135.7681);
        assertEquals(ab, ba, 1e-9);
    }

    @Test
    void knownDistanceLondonToParisIsAbout344Km() {
        double d = Geo.haversineKm(51.5074, -0.1278, 48.8566, 2.3522);
        assertEquals(344, d, 5);
    }

    @Test
    void oneDegreeOfLatitudeIsAbout111Km() {
        assertEquals(111.2, Geo.haversineKm(0, 0, 1, 0), 0.5);
    }
}
