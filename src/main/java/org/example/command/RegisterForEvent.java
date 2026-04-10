package org.example.command;

import org.example.service.RegistrationService;

public class RegisterForEvent implements RegistrationCommand {

    private final RegistrationService service;
    private final String eventId;
    private final String userId;

    public RegisterForEvent(RegistrationService service, String eventId, String userId) {
        this.service = service;
        this.eventId = eventId;
        this.userId = userId;
    }

    @Override
    public void execute() {
        service.register(eventId, userId);
    }

}
