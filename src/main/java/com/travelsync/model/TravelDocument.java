package com.travelsync.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A travel document (visa, itinerary letter, insurance certificate, ticket) tied to a booking.
 * The free-text `content` field is the corpus used by the suffix-structure (M2) search demos.
 */
public class TravelDocument {
    private final String id;
    private String type;          // Visa, Itinerary, Insurance, Ticket
    private String travelerId;
    private String bookingId;
    private String content;
    private LocalDate issuedOn;
    private LocalDate expiresOn;
    private boolean processed;

    public TravelDocument(String id, String type, String travelerId, String bookingId,
                           String content, LocalDate issuedOn, LocalDate expiresOn, boolean processed) {
        this.id = id;
        this.type = type;
        this.travelerId = travelerId;
        this.bookingId = bookingId;
        this.content = content;
        this.issuedOn = issuedOn;
        this.expiresOn = expiresOn;
        this.processed = processed;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getTravelerId() { return travelerId; }
    public String getBookingId() { return bookingId; }
    public String getContent() { return content; }
    public LocalDate getIssuedOn() { return issuedOn; }
    public LocalDate getExpiresOn() { return expiresOn; }
    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TravelDocument)) return false;
        return Objects.equals(id, ((TravelDocument) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return type + " " + id; }
}
