package org.example.service;

import javafx.stage.Stage;
import org.example.domain.Feature;
import org.example.domain.Role;
import org.example.domain.User;
import org.example.repository.InMemoryUserRepo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Optional;
import java.util.Set;

public class Auth {

    private User currentUser;

    public Auth() {
    }

    public boolean authUser(String username, String password) {

        InMemoryUserRepo userRepo = new InMemoryUserRepo();
        JSONArray userInfo = new JSONArray();
        String userRole = "";
        String userId = "";

        // Store the contents of the users.json file into the JSONArray variable
        userInfo = userRepo.readUserJSONFile();
        // Declare and initialize a variable to track if user exists in the system
        boolean userFound = false;

        // Use for-loop to iterate through each user to determine if the provided credentials match an existing user
        for (int i = 0; i < userInfo.length(); i++) {
            JSONObject user = userInfo.getJSONObject(i);

            // Use if statement to determine if the username and password match
            if (user.getString("username").equals(username) && user.getString("password").equals(password)) {
                // If match is found, set variable to true
                userFound = true;
                // Store the users role to be used in the Access Control
                userRole = user.getString("role");
                userId = user.getString("id");
                establishNewUserSession(userId, username, userRole);
            }
        }

        return userFound;
    }

    // Getter for the User
    public User getCurrentUser() {
        return currentUser;
    }

    private void establishNewUserSession(String userId, String username, String userRole) {
        // Declare and initialize basic role for a user
        Role role = Role.STUDENT;

        // Use if-else statements to set the appropriate role for the user
        if (userRole.equals("admin")) {
            role = Role.ADMIN;
        } else if (userRole.equals("clubLeader")) {
            role = Role.CLUB_LEADER;
        } else if (userRole.equals("student")) {
            role = Role.STUDENT;
        }

        // Create a new user with provided username and determined role
        User user = new User(userId, username, role);
        this.currentUser = user;

        // Declare new AccessControl to determine the allowed features for the particular user role
        AccessControl accessControl = new AccessControl();
        Stage stage = new Stage();
        accessControl.buildFeatureUI(stage, user);
    }
}
