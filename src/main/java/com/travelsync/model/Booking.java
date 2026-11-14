package com.travelsync.model;

import java.time.LocalDate;
import java.util.Objects;

public class Booking {

    public enum Status { PENDING, CONFIRMED, CANCELLED, COMPLETED }

    private final String id;          // booking code, e.g. "BK-7F2A19" - searchable via Rabin-Karp
    private String travelerId;
    private String destinationId;
    private String hotelId;
    private String transportationId;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalCost;
    private Status status;
    private LocalDate createdOn;

    public Booking(String id, String travelerId, String destinationId, String hotelId,
                    String transportationId, LocalDate startDate, LocalDate endDate,
                    double totalCost, Status status, LocalDate createdOn) {
        this.id = id;
        this.travelerId = travelerId;
        this.destinationId = destinationId;
        this.hotelId = hotelId;
        this.transportationId = transportationId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCost = totalCost;
        this.status = status;
        this.createdOn = createdOn;
    }

    public String getId() { return id; }
    public String getTravelerId() { return travelerId; }
    public String getDestinationId() { return destinationId; }
    public String getHotelId() { return hotelId; }
    public String getTransportationId() { return transportationId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public double getTotalCost() { return totalCost; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDate getCreatedOn() { return createdOn; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        return Objects.equals(id, ((Booking) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return id + " [" + status + "]"; }
}
