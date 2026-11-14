package com.travelsync.model;

import java.util.Objects;

public class Attraction {
    private final String id;
    private String name;
    private String destinationId;
    private String type;          // Museum, Landmark, Nature, Adventure, Cultural
    private String description;
    private double entryFee;
    private double rating;
    private int visitDurationMinutes;

    public Attraction(String id, String name, String destinationId, String type,
                       String description, double entryFee, double rating, int visitDurationMinutes) {
        this.id = id;
        this.name = name;
        this.destinationId = destinationId;
        this.type = type;
        this.description = description;
        this.entryFee = entryFee;
        this.rating = rating;
        this.visitDurationMinutes = visitDurationMinutes;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDestinationId() { return destinationId; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public double getEntryFee() { return entryFee; }
    public double getRating() { return rating; }
    public int getVisitDurationMinutes() { return visitDurationMinutes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attraction)) return false;
        return Objects.equals(id, ((Attraction) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return name; }
}
