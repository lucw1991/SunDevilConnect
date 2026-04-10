package org.example.service;

import org.example.domain.Event;
import org.example.observer.Observer;
import org.example.observer.Subject;
import org.example.repository.EventRepo;
import org.example.strategy.SortStrat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventCatalogService implements Subject {

    private final EventRepo events;
    private final Set<Observer> observers = new HashSet<Observer>();
    private SortStrat sortStrat;

    public EventCatalogService(EventRepo events, SortStrat sortStrat) {
        this.events = events;
        this.sortStrat = sortStrat;
    }

    public void setSortStrat(SortStrat sortStrat) {
        this.sortStrat = sortStrat;
    }

    public List<Event> listAll() {
        List<Event> copy = new ArrayList<>(events.findAll());
        if (sortStrat != null) {
            sortStrat.sort(copy);
        }
        return copy;
    }

    public List<Event> listByCategory(String category) {
        // Treat null or ALL as no filter
        if (category == null || category.equalsIgnoreCase("ALL")) {
            return listAll();
        }

        List<Event> filtered = new ArrayList<>();
        for (Event e : events.findAll()) {
            if (category.equalsIgnoreCase(e.getCategory())) {
                filtered.add(e);
            }
        }

        if (sortStrat != null) {
            sortStrat.sort(filtered);
        }

        return filtered;
    }

    public List<Event> listByClub(String clubId) {
        List<Event> results = new ArrayList<>();
        for (Event e : events.findAll()) {
            if (clubId.equals(e.getClubId())) {
                results.add(e);
            }
        }

        if (sortStrat != null) {
            sortStrat.sort(results);
        }
        return results;
    }

    public Event add(Event e) {
        Event saved = events.save(e);
        notifyObservers("New event posted: " + e.getTitle());
        return saved;
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
