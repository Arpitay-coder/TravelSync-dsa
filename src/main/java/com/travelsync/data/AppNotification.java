package com.travelsync.data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppNotification {
    private final String title;
    private final String message;
    private final LocalDateTime timestamp;
    private boolean read;

    public AppNotification(String title, String message, LocalDateTime timestamp) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.read = false;
    }

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("MMM d, HH:mm"));
    }
}
