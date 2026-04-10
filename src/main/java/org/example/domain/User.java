package org.example.domain;

import java.util.UUID;

public class User {

    private final String id;
    private final String name;
    private final Role role;

    public User(String userId, String name, Role role) {
        this.id = userId;
        this.name = name;
        this.role = role;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public Role getRole() {
        return role;
    }

}
