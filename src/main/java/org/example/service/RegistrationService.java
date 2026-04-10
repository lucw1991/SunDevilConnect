package org.example.service;

import org.example.domain.Event;
import org.example.domain.Registration;
import org.example.observer.Observer;
import org.example.observer.Subject;
import org.example.repository.EventRepo;
import org.example.repository.InMemoryEventRepo;
import org.example.repository.RegistrationRepo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public class RegistrationService implements Subject {

    private final RegistrationRepo registrations;
    private final EventRepo events;
    private final Set<Observer> observers = new HashSet<>();

    public RegistrationService(RegistrationRepo registrations, EventRepo events) {
        this.registrations = registrations;
        this.events = events;
    }

    public Registration register(String eventId, String userId) {
        Event e = events.findById(eventId)
                        .orElseThrow(() -> new NoSuchElementException("Event not found."));
        long current = registrations.findByEventId(eventId)
                                    .stream()
                                    .filter(r ->
                                                r.getStatus() == Registration.Status.REGISTERED)
                                    .count();

        if (current >= e.getCapacity()) {
            notifyObservers("Unable to process registration, event is full!");
        } else {
            System.out.println("Registration processing...");
            InMemoryEventRepo inMemoryEventRepo = new InMemoryEventRepo();
            JSONArray events = inMemoryEventRepo.readEventsJSONFile();
            JSONArray registrants = new JSONArray();
            JSONObject event = new JSONObject();

            for (int i = 0; i < events.length(); i++) {
                event = events.getJSONObject(i);

                if (event.optString("id").equals(eventId)) {
                    System.out.println("Before registration: " + event);

                    registrants = event.getJSONArray("registrants");
//                     if (event.has("registrants")) {
//                         registrants = event.getJSONArray("registrants");
//                     } else if (event.has("registrations")) {
//                         registrants = event.getJSONArray("registrations");
//                    } else {
//                         registrants = new JSONArray();
//                         event.put("registrants", registrants);
//                    }
                    break;
                }
            }

            // Add user to the list
            boolean registered = false;
            for (int j = 0; j < registrants.length(); j++) {
                String regUser = registrants.getString(j);

                if (regUser.equals(userId)) {
                    registered = true;
                    break;
                }
            }

            if (!registered) {
                event.getJSONArray("registrants").put(userId);
                int capacity = event.getInt("capacity");
                capacity--;
                event.put("capacity", capacity);
                System.out.println("After registration: " + event);

                notifyObservers("Registration confirmed for event: " + e.getTitle());
                inMemoryEventRepo.writeEventsJSONFile(events);
            } else {
                notifyObservers("Already registered for event: " + e.getTitle());
            }
        }

        Registration r = registrations.save(new Registration(eventId, userId));
        return r;
    }

    public void cancel(Registration r) {
        r.cancel();
        Event e = events.findById(r.getEventId()).orElseThrow();
        notifyObservers("Registration cancelled for event: " + e.getTitle());
    }

    // Getter for User registrations
    public List<Registration> getUserReg(String userId) {
        return registrations.findByUserId(userId);
    }

    @Override
    public void attach(Observer o) {
        observers.add(o);
    }

    @Override
    public void detach(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(String message) {
        observers.forEach(o -> o.update(message));
    }

}
