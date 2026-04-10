package org.example.strategy;

import org.example.domain.Event;

import java.util.Comparator;
import java.util.List;

public class CostSort implements SortStrat {

    @Override
    public List<Event> sort(List<Event> events) {
        events.sort(Comparator.comparingDouble(Event::getCost));
        return events;
    }

}
