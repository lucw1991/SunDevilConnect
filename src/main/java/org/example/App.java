package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.service.Auth;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class App extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("SunDevilConnect - Login");

        // Set up collection of login credentials
        var credentials = new VBox(12);
        credentials.setPadding(new Insets(24));
        credentials.setAlignment(Pos.CENTER);

        var userName = new HBox(12);
        var userNameLabel = new Label("Username:");
        var userNameField = new TextField();
        userNameField.setPromptText("Enter username");
        userName.setAlignment(Pos.CENTER);

        userName.getChildren().addAll(userNameLabel, userNameField);

        var password = new HBox(12);
        var passwordLabel = new Label("Password:");
        var passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        password.setAlignment(Pos.CENTER);

        password.getChildren().addAll(passwordLabel, passwordField);

        var loginBtn = new Button("Login");
        loginBtn.setAlignment(Pos.CENTER);

        var error = new VBox(12);
        error.setAlignment(Pos.CENTER);
        var errorMsg = new Label("LOGIN FAILED: Incorrect username or password.");
        var successMsg = new Label("LOGIN SUCCESSFUL: Correct username and password.");
        errorMsg.setVisible(false);
        successMsg.setVisible(false);
        error.getChildren().addAll(errorMsg, successMsg);

        // Initialize authentication when user submits login credentials
        loginBtn.setOnAction(e -> {
            errorMsg.setVisible(false);
            successMsg.setVisible(false);
            Auth auth = new Auth();
            boolean userAuth = auth.authUser(userNameField.getText(), passwordField.getText());

            if (!userAuth) {
                errorMsg.setVisible(true);
            } else {
                stage.hide();
            }
        });

        credentials.getChildren().addAll(userName, password, loginBtn, error);

        var scene  = new Scene(new StackPane(credentials), 480, 320);
        stage.setScene(scene);
        stage.show();
    }

}

