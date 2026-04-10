package org.example.command;

import org.example.service.RegistrationService;
import org.example.service.SignUpService;

public class ClubSignUp implements SignUpCommand{

    private final SignUpService service;
    private final String clubId;
    private final String userId;

    public ClubSignUp(SignUpService service, String clubId, String userId) {
        this.service = service;
        this.clubId = clubId;
        this.userId = userId;
    }

    @Override
    public void execute() {
        service.signUp(clubId, userId);
    }
}
