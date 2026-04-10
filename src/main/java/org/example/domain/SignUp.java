package org.example.domain;

import java.util.UUID;

public class SignUp {

    private final String clubId;
    private final String userId;

    public SignUp(String clubId, String userId) {
        this.clubId = clubId;
        this.userId = userId;
    }

    public String getClubId() { return clubId; }
    public String getUserId() {
        return userId;
    }

}
