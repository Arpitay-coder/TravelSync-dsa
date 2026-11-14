package com.travelsync.model;

import java.util.Objects;

/**
 * Represents a travel destination (city / region / landmark area) within TravelSync.
 */
public class Destination {

    private final String id;
    private String name;
    private String country;
    private String region;
    private String description;
    private String category;      // e.g. Beach, Mountain, Heritage, Urban, Wildlife
    private double popularityScore; // 0-100, used by ranking/QuickSort demos
    private double averageRating;   // 0-5
    private int annualVisitors;
    private double latitude;
    private double longitude;

    public Destination(String id, String name, String country, String region,
                        String description, String category, double popularityScore,
                        double averageRating, int annualVisitors,
                        double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.region = region;
        this.description = description;
        this.category = category;
        this.popularityScore = popularityScore;
        this.averageRating = averageRating;
        this.annualVisitors = annualVisitors;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPopularityScore() { return popularityScore; }
    public void setPopularityScore(double popularityScore) { this.popularityScore = popularityScore; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public int getAnnualVisitors() { return annualVisitors; }
    public void setAnnualVisitors(int annualVisitors) { this.annualVisitors = annualVisitors; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Destination)) return false;
        Destination that = (Destination) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return name + " (" + country + ")";
    }
}
