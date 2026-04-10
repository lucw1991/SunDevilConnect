package org.example.strategy;

import org.example.domain.Event;

import java.util.Comparator;
import java.util.List;

public class PopularitySort implements SortStrat {

    @Override
    public List<Event> sort(List<Event> events) {
        events.sort(Comparator.comparing(Event::getPopularity).reversed());
        return events;
    }

}
