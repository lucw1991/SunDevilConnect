package org.example.repository;

import org.example.domain.Registration;
import java.util.List;

public interface RegistrationRepo {

    Registration save(Registration r);
    List<Registration> findByEventId(String eventId);
    List<Registration> findByUserId(String userId);

}
