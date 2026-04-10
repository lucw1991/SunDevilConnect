package org.example.domain;

public class Club {

    private final String id;
    private final String name;
    private final String description;

    public Club(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }


    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }

}
