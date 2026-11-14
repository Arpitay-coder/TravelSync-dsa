package com.travelsync.model;

import java.util.Objects;

public class Traveler {
    private final String id;
    private String fullName;
    private String email;
    private String nationality;
    private int loyaltyPoints;
    private String preferredCategory; // used for bipartite matching to eligible hotels

    public Traveler(String id, String fullName, String email, String nationality,
                     int loyaltyPoints, String preferredCategory) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.nationality = nationality;
        this.loyaltyPoints = loyaltyPoints;
        this.preferredCategory = preferredCategory;
    }

    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getNationality() { return nationality; }
    public int getLoyaltyPoints() { return loyaltyPoints; }
    public String getPreferredCategory() { return preferredCategory; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Traveler)) return false;
        return Objects.equals(id, ((Traveler) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return fullName; }
}
