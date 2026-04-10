package org.example.repository;

import org.example.domain.Registration;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class InMemoryRegistrationRepo implements RegistrationRepo {

    private final Map<String, Registration> store = new HashMap<>();

    @Override
    public Registration save(Registration r) {
        store.put(r.getEventId(), r);
        return r;
    }

    @Override
    public List<Registration> findByEventId(String eventId) {
        return store.values().stream().filter(r -> r.getEventId()
                                                               .equals(eventId))
                                                               .collect(Collectors.toList());
    }

    @Override
    public List<Registration> findByUserId(String userId) {
        return store.values().stream().filter(r -> r.getUserId()
                                                               .equals(userId))
                                                               .collect(Collectors.toList());
    }

}
