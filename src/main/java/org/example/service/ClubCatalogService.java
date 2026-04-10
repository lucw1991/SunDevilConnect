package org.example.service;

import org.example.domain.Club;
import org.example.repository.ClubRepo;

import java.util.List;

public class ClubCatalogService {

    private final ClubRepo clubs;

    public ClubCatalogService(ClubRepo clubs) {
        this.clubs = clubs;
    }

    public List<Club> listAll() {
        return clubs.findAll();
    }

}
