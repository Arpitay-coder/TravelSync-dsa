package com.travelsync.model;

import java.util.Objects;

/**
 * A transport link between two destinations. Doubles as an edge in the
 * network-flow graphs built for M4 (capacity = seats/slots available).
 */
public class Transportation {
    private final String id;      // e.g. transport route code, searchable
    private String mode;          // Flight, Train, Bus, Ferry
    private String sourceDestinationId;
    private String targetDestinationId;
    private int capacity;         // total seats/slots (flow network capacity)
    private int booked;           // currently booked
    private double price;
    private int durationMinutes;

    public Transportation(String id, String mode, String sourceDestinationId,
                           String targetDestinationId, int capacity, int booked,
                           double price, int durationMinutes) {
        this.id = id;
        this.mode = mode;
        this.sourceDestinationId = sourceDestinationId;
        this.targetDestinationId = targetDestinationId;
        this.capacity = capacity;
        this.booked = booked;
        this.price = price;
        this.durationMinutes = durationMinutes;
    }

    public String getId() { return id; }
    public String getMode() { return mode; }
    public String getSourceDestinationId() { return sourceDestinationId; }
    public String getTargetDestinationId() { return targetDestinationId; }
    public int getCapacity() { return capacity; }
    public int getBooked() { return booked; }
    public void setBooked(int booked) { this.booked = booked; }
    public int getAvailableCapacity() { return capacity - booked; }
    public double getPrice() { return price; }
    public int getDurationMinutes() { return durationMinutes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transportation)) return false;
        return Objects.equals(id, ((Transportation) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return mode + " " + id + " (" + sourceDestinationId + " -> " + targetDestinationId + ")";
    }
}
