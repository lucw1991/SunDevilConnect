package org.example.repository;

import org.example.domain.User;
import java.util.List;
import java.util.Optional;

public interface UserRepo {

    User save(User u);
    Optional<User> findById(String id);
    List<User> findAll();

}
