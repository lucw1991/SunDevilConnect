package org.example.repository;

import org.example.domain.Club;

import java.util.List;
import java.util.Optional;

public interface ClubRepo {

    Club save(Club club);
    Optional<Club> findById(String id);
    List<Club> findAll();

}
