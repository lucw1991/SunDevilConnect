package org.example.domain;

import java.time.Instant;

public class Notification {

    private final String recipientUserId;
    private final String message;
    private final Instant createdAt = Instant.now();

    public Notification(String recipientUserId, String message) {
        this.recipientUserId = recipientUserId;
        this.message = message;
    }

    public String getRecipientUserId() {
        return recipientUserId;
    }
    public String getMessage() {
        return message;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }

}
