package org.example.api;
import org.example.command.SignUpCommand;

public class SignUpAPI {

    public void submit(SignUpCommand r) {
        r.execute();
    }
}
