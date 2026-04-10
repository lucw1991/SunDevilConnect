package org.example.service;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.api.ClubActivityAPI;
import org.example.api.ClubCatalogAPI;
import org.example.api.EventCatalogAPI;
import org.example.api.RegistrationAPI;
import org.example.api.SignUpAPI;
import org.example.ui.*;
import org.example.domain.*;
import org.example.repository.*;
import org.example.strategy.DateSort;

import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.EnumMap;
import java.util.Set;
import java.util.EnumSet;



public class AccessControl {

    private final Map<Role, Set<Feature>> grants = new EnumMap<>(Role.class);

    // Services and APIs used by Events UI
    private final EventCatalogAPI eCatAPI;
    private final RegistrationAPI regAPI;
    private final RegistrationService regService;

    // For Clubs UI
    private final ClubCatalogAPI cCatAPI;
    private final SignUpAPI signUpAPI;
    private final SignUpService signUpService;

    // For club activity
    private final ClubActivityAPI clubActivityAPI;

    public AccessControl() {

        // Student role
        grants.put(Role.STUDENT, EnumSet.of(Feature.VIEW_EVENTS, Feature.REGISTER_FOR_EVENT, Feature.VIEW_CLUBS));

        // Club leader
        grants.put(Role.CLUB_LEADER, EnumSet.of(Feature.VIEW_EVENTS, Feature.REGISTER_FOR_EVENT,
                   Feature.CREATE_EVENT,Feature.VIEW_CLUBS, Feature.MANAGE_MEMBERS));

        // Admin
        grants.put(Role.ADMIN, EnumSet.of(Feature.VIEW_EVENTS, Feature.REGISTER_FOR_EVENT,
                                          Feature.CREATE_EVENT, Feature.VIEW_CLUBS, Feature.MANAGE_MEMBERS,
                                          Feature.ADMIN_DASHBOARD));

        // Initialize things for events and clubs
        InMemoryEventRepo eventRepo = new InMemoryEventRepo();
        RegistrationRepo registrationRepo = new InMemoryRegistrationRepo();
        InMemoryClubRepo clubRepo = new InMemoryClubRepo();
        SignUpRepo signUpRepo = new InMemorySignUpRepo();

        // Load from JSON
        seedEvents(eventRepo);
        seedClubs(clubRepo);

        var eCatService = new EventCatalogService(eventRepo, new DateSort());
        var cCatService = new  ClubCatalogService(clubRepo);

        this.regService = new RegistrationService(registrationRepo, eventRepo);
        this.eCatAPI = new EventCatalogAPI(eCatService);
        this.regAPI = new RegistrationAPI();
        this.cCatAPI = new ClubCatalogAPI(cCatService);
        this.signUpAPI = new SignUpAPI();
        this.signUpService = new SignUpService(signUpRepo, clubRepo);

        // Wire club activity service and API
        ClubActivityService clubActivityService = new ClubActivityService();
        this.clubActivityAPI = new ClubActivityAPI(clubActivityService);

    }

    public Boolean allowed(User u, Feature f) {
        return grants.getOrDefault(u.getRole(), Collections.emptySet()).contains(f);
    }

    public Set<Feature> featuresFor(User u) {
        return Collections.unmodifiableSet(grants.getOrDefault(u.getRole(), Collections.emptySet()));
    }

    public void buildFeatureUI(Stage stage, User user) {
        stage.setTitle("SunDevilConnect");

        // Set up features for user to access
        var featureSelect = new VBox(12);
        featureSelect.setPadding(new Insets(24));
        featureSelect.setAlignment(Pos.CENTER);

        // Add a hello and welcome message component to greet the user
        var helloMsg = new Label("Hello, " + user.getName() + "!");
        helloMsg.setAlignment(Pos.CENTER);
        var welcomeMsg = new Label("Welcome to the SunDevilConnect System");
        welcomeMsg.setPadding(new Insets(24));
        welcomeMsg.setAlignment(Pos.CENTER);

        // Establish buttons for all features of the system
        var profile = new Button("View Profile");
        var regEvent = new Button("View Events");
        var clubSignup = new Button("View Clubs");
        var createEvent = new Button("Create Event");
        var adminManage = new Button("Manage Clubs, Events, Announcements");
        var adminDashboard = new Button("Admin Dashboard");
        var backBtn = new Button("Back");

        // Use if-else statement to determine role that the user has and add the features buttons that the user has
        // permission to access
        if (user.getRole() == Role.ADMIN) {
            featureSelect.getChildren().addAll(helloMsg, welcomeMsg, profile, regEvent, clubSignup, createEvent, adminManage, adminDashboard);
        } else if (user.getRole() == Role.CLUB_LEADER) {
            featureSelect.getChildren().addAll(helloMsg, welcomeMsg, profile, regEvent, clubSignup, createEvent,  adminManage);
        } else {
            featureSelect.getChildren().addAll(helloMsg, welcomeMsg, profile, regEvent, clubSignup);
        }

        /* ********** Handle action on button clicks ********** */

        // Register
        regEvent.setOnAction(e -> {
            // Build the ui
            Events eView = new Events(eCatAPI, regAPI, regService, user);

            // Back button
            backBtn.setOnAction(ev -> buildFeatureUI(stage, user));
            backBtn.setPadding(new Insets(8));

            BorderPane page = new BorderPane();
            page.setTop(backBtn);
            BorderPane.setMargin(backBtn, new Insets(8));
            page.setCenter(eView);

            Scene eScene = new Scene(page, 960, 640);
            stage.setScene(eScene);
        });

        // Club Signup
        clubSignup.setOnAction(e -> {
           Clubs cView = new Clubs(cCatAPI, signUpAPI, signUpService, eCatAPI, clubActivityAPI, user);


           backBtn.setOnAction(ev -> buildFeatureUI(stage, user));
           backBtn.setPadding(new Insets(8));

           BorderPane page = new BorderPane();
           page.setTop(backBtn);
           BorderPane.setMargin(backBtn, new Insets(8));
           page.setCenter(cView);

           Scene cScene = new Scene(page, 960, 640);
           stage.setScene(cScene);
        });

        // Create Event
         createEvent.setOnAction(e -> {
             CreateEvents ceView = new CreateEvents();

             backBtn.setOnAction(ev -> buildFeatureUI(stage, user));
             backBtn.setPadding(new Insets(8));

             BorderPane page = new BorderPane();
             page.setTop(backBtn);
             BorderPane.setMargin(backBtn, new Insets(8));
             page.setCenter(ceView);

             Scene ceScene = new Scene(page, 960, 640);
             stage.setScene(ceScene);
         });

         // View profile
        profile.setOnAction(e -> {
           Profile pView = new Profile(user, regService, signUpService, eCatAPI, cCatAPI);

           backBtn.setOnAction(ev -> buildFeatureUI(stage, user));
           backBtn.setPadding(new Insets(8));

           BorderPane page = new BorderPane();
           page.setTop(backBtn);
           BorderPane.setMargin(backBtn, new Insets(8));
           page.setCenter(pView);

           Scene pScene = new Scene(page, 960, 640);
           stage.setScene(pScene);
        });

        // Admin Dashboard
        adminDashboard.setOnAction(e -> {
            AdminDashboard adView = new AdminDashboard(user.getId());

            backBtn.setOnAction(ev -> buildFeatureUI(stage, user));
            backBtn.setPadding(new Insets(8));

            BorderPane page = new BorderPane();
            page.setTop(backBtn);
            BorderPane.setMargin(backBtn, new Insets(8));
            page.setCenter(adView);

            Scene adScene = new Scene(page, 960, 640);
            stage.setScene(adScene);
        });

        // Manage UI for Admin
        adminManage.setOnAction(e -> {
           AdminManage amView = new AdminManage(user, user.getId());

           backBtn.setOnAction(ev -> buildFeatureUI(stage, user));
           backBtn.setPadding(new Insets(8));

           BorderPane page = new BorderPane();
           page.setTop(backBtn);
           BorderPane.setMargin(backBtn, new Insets(8));
           page.setCenter(amView);

           Scene amScene = new Scene(page, 960, 640);
           stage.setScene(amScene);
        });

        var scene  = new Scene(new StackPane(featureSelect), 960, 640);
        stage.setScene(scene);
        stage.show();
    }


    // Get Events from the JSON file
    private void seedEvents(InMemoryEventRepo eventRepo) {
        JSONArray eArray = eventRepo.readEventsJSONFile();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("MM-dd-yyyy");

        for (int i = 0; i < eArray.length(); i++) {
            JSONObject obj = eArray.getJSONObject(i);

            String eventId = obj.getString("id");
            String clubId = obj.getString("clubId");

            String title = obj.optString("title", obj.optString("tile", "Untitled Event"));
            String dateStr = obj.getString("date");
            LocalDate date = LocalDate.parse(dateStr, df);

            // These aren't in JSON yet but I feel like we will want to use them as we continue along
            String location = "TBD";

            double cost = obj.getDouble("cost");
            int popularity = obj.getInt("popularity");
            int capacity = obj.getInt("capacity");
            String category = obj.getString("category");

            eventRepo.save(new Event(eventId, clubId, title, date, location, cost, popularity, capacity, category));
        }
    }

    // Get Clubs from JSON
    private void seedClubs(InMemoryClubRepo clubRepo) {
        JSONArray clubsArray = clubRepo.readClubs();

        for (int i  = 0; i < clubsArray.length(); i++) {
            JSONObject obj = clubsArray.getJSONObject(i);

            String id = obj.optString("id", "");
            String name = obj.optString("name", "Unnamed Club");
            String description = obj.optString("description", "");

            clubRepo.save(new Club(id, name, description));
        }
    }
}
