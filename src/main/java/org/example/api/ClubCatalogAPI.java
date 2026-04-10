package org.example.api;

import org.example.domain.Club;
import org.example.service.ClubCatalogService;

import java.util.List;

public class ClubCatalogAPI {

    private final ClubCatalogService catalog;

    public ClubCatalogAPI(ClubCatalogService catalog) {
        this.catalog = catalog;
    }

    public List<Club> listClubs() {
        return catalog.listAll();
    }

}
