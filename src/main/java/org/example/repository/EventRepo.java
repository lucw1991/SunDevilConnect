package org.example.repository;

import org.example.domain.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepo {

    Event save(Event event);
    Optional<Event> findById(String id);
    List<Event> findAll();

}
