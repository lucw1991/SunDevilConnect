package org.example.api;

import org.example.domain.Event;
import org.example.service.EventCatalogService;
import org.example.strategy.SortStrat;

import java.util.List;

public class EventCatalogAPI {

    private final EventCatalogService catalog;

    public EventCatalogAPI(EventCatalogService catalog) {
        this.catalog = catalog;
    }

    public List<Event> listEvents() {
        return catalog.listAll();
    }

    public List<Event> listEventsByCategory(String category) {
        return catalog.listByCategory(category);
    }

    public List<Event> listEventsForClub(String clubId) {
        return catalog.listByClub(clubId);
    }

    public void setSortStrat(SortStrat s) {
        catalog.setSortStrat(s);
    }

}
