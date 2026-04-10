package org.example.strategy;

import org.example.domain.Event;
import java.util.List;

public interface SortStrat {
    List<Event> sort(List<Event> events);
}
