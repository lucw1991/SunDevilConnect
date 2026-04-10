package org.example.api;

import org.example.command.RegistrationCommand;

public class RegistrationAPI {

    public void submit(RegistrationCommand r) {
        r.execute();
    }

}
