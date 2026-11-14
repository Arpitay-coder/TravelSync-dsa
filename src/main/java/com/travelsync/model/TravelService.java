package com.travelsync.model;

import java.util.Objects;

/**
 * An ancillary service offered at a destination (guided tour, airport transfer, spa, etc).
 * Used as the "eligible service" side of the bipartite matching demo (M4) and as
 * search/index content for M1/M2 demos.
 */
public class TravelService {
    private final String id;
    private String name;
    private String category;      // Tour, Transfer, Wellness, Dining, Adventure
    private String destinationId;
    private double price;
    private int capacityPerDay;
    private double rating;

    public TravelService(String id, String name, String category, String destinationId,
                          double price, int capacityPerDay, double rating) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.destinationId = destinationId;
        this.price = price;
        this.capacityPerDay = capacityPerDay;
        this.rating = rating;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDestinationId() { return destinationId; }
    public double getPrice() { return price; }
    public int getCapacityPerDay() { return capacityPerDay; }
    public double getRating() { return rating; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TravelService)) return false;
        return Objects.equals(id, ((TravelService) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return name; }
}
