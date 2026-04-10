package org.example.observer;

import org.example.domain.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationService implements Observer {

    private final List<Notification> outbox = new ArrayList<>();
    private final String recipientUserId;

    public NotificationService(String recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    @Override
    public void update(String message) {
        outbox.add(new Notification(recipientUserId, message));
    }

    public List<Notification> flush() {
        return new ArrayList<>(outbox);
    }

}
