package org.example.strategy;

import org.example.domain.Event;

import java.util.Comparator;
import java.util.List;

public class DateSort implements SortStrat {

    @Override
    public List<Event> sort(List<Event> events) {
        events.sort(Comparator.comparing(Event::getDateTime));
        return events;
    }

}
