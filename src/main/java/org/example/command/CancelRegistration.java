package org.example.command;

import org.example.domain.Registration;
import org.example.service.RegistrationService;

public class CancelRegistration implements RegistrationCommand {

    private final RegistrationService service;
    private final Registration registration;

    public CancelRegistration(RegistrationService service, Registration registration) {
        this.service = service;
        this.registration = registration;
    }

    @Override
    public void execute() {
        service.cancel(registration);
    }

}
