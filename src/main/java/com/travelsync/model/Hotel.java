package com.travelsync.model;

import java.util.Objects;

public class Hotel {
    private final String id;
    private String name;
    private String destinationId;
    private int starRating;       // 1-5
    private double pricePerNight;
    private int totalRooms;
    private int availableRooms;
    private String[] amenities;
    private double guestRating;   // 0-5

    public Hotel(String id, String name, String destinationId, int starRating,
                 double pricePerNight, int totalRooms, int availableRooms,
                 String[] amenities, double guestRating) {
        this.id = id;
        this.name = name;
        this.destinationId = destinationId;
        this.starRating = starRating;
        this.pricePerNight = pricePerNight;
        this.totalRooms = totalRooms;
        this.availableRooms = availableRooms;
        this.amenities = amenities;
        this.guestRating = guestRating;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDestinationId() { return destinationId; }
    public int getStarRating() { return starRating; }
    public double getPricePerNight() { return pricePerNight; }
    public int getTotalRooms() { return totalRooms; }
    public int getAvailableRooms() { return availableRooms; }
    public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }
    public String[] getAmenities() { return amenities; }
    public double getGuestRating() { return guestRating; }

    public double getUtilization() {
        if (totalRooms == 0) return 0;
        return 1.0 - ((double) availableRooms / totalRooms);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hotel)) return false;
        return Objects.equals(id, ((Hotel) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return name; }
}
