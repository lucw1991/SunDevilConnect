package org.example.domain;

import java.util.UUID;

public class Registration {

    public enum Status { REGISTERED, CANCELLED }
    
    private final String eventId;
    private final String userId;
    private Status status;

    public Registration(String eventId, String userId) {
        this.eventId = eventId;
        this.userId = userId;
        this.status = Status.REGISTERED;
    }

    public String getEventId() {
        return eventId;
    }
    public String getUserId() {
        return userId;
    }
    public Status getStatus() {
        return status;
    }

    public void cancel() { this.status = Status.CANCELLED; }

}
