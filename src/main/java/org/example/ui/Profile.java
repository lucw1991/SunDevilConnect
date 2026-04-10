package org.example.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.api.ClubCatalogAPI;
import org.example.api.EventCatalogAPI;
import org.example.domain.*;

import javafx.scene.control.Label;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.example.repository.InMemoryClubRepo;
import org.example.repository.InMemoryEventRepo;
import org.example.service.RegistrationService;
import org.example.service.SignUpService;
import org.json.JSONArray;
import org.json.JSONObject;


public class Profile extends BorderPane {

    private final Label details = new Label("Select an event for details.");
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final TextArea notifications = new TextArea();

    public Profile(User user,
                   RegistrationService regService,
                   SignUpService signUpService,
                   EventCatalogAPI eventCatAPI,
                   ClubCatalogAPI clubCatAPI) {

        // Top for user info
        VBox userBox = new VBox(8);
        userBox.setPadding(new Insets(20));
        userBox.setAlignment(Pos.CENTER);

        Label title = new Label("Profile");
        title.setStyle("-fx-font-size: 24");
        Label name = new Label("Name: " + user.getName());
        Label role = new Label("Role: " + user.getRole());

        userBox.getChildren().addAll(title, name, role);
        setTop(userBox);

        // Center for events and clubs
        HBox listsBox = new HBox(40);
        listsBox.setPadding(new Insets(20));

        // All of the User's registrations
        List<Registration> regs = regService.getUserReg(user.getId());

        // Generate User's registrations from previous sessions
        InMemoryEventRepo eventRepo = new InMemoryEventRepo();
        JSONArray events = new JSONArray();

        events = eventRepo.readEventsJSONFile();

        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.getJSONObject(i);
            JSONArray registrations = event.getJSONArray("registrants");

            for (int j = 0; j < registrations.length(); j++) {
                String registrant = registrations.getString(j);

                if (registrant.equals(user.getId())) {
                    Registration newReg = new Registration(event.getString("id"), user.getId());
                    regs.add(newReg);
                }
            }
        }

        // Look up all of the events once by ID
        Map<String, Event> eventsByID = eventCatAPI.listEvents()
                                                   .stream()
                                                   .collect(Collectors.toMap(Event::getId, e -> e));

        // Events list
        VBox eventBox = new VBox(8);
        Label eventsLabel = new Label("Events Registered For:");

        List<Event> registeredEvents = regs.stream()
                                           .map(Registration::getEventId)
                                           .map(eventsByID::get)
                                           .filter(Objects::nonNull)
                                           .distinct()
                                           .toList();

        ListView<String> eventsList = new ListView<>();
        eventsList.getItems().addAll(regs.stream()
                                         .map(Registration::getEventId)
                                         .map(eventsByID::get)
                                         .filter(Objects::nonNull)
                                         .map(Event::getTitle)
                                         .distinct()
                                         .toList());

        eventBox.getChildren().addAll(eventsLabel, eventsList);

        // Clubs list
        VBox clubBox = new VBox(8);
        Label clubsLabel = new Label("Your Clubs:");

        List<SignUp> clubs = signUpService.getUserSignUp(user.getId());

        InMemoryClubRepo clubRepo = new InMemoryClubRepo();
        JSONArray getClubs = new JSONArray();

        getClubs = clubRepo.readClubs();

        for (int i = 0; i < getClubs.length(); i++) {
            JSONObject club = getClubs.getJSONObject(i);
            JSONArray members = club.getJSONArray("members");

            for (int j = 0; j < members.length(); j++) {
                String member = members.getString(j);

                if (member.equals(user.getId())) {
                    SignUp newSignUp = new SignUp(club.getString("id"), user.getId());
                    clubs.add(newSignUp);
                }
            }
        }

        Map<String, Club> clubsById = clubCatAPI.listClubs()
                                            .stream()
                                           .collect(Collectors.toMap(Club::getId, c -> c));

        List<Club> registeredClubs = clubs.stream()
                                         .map(SignUp::getClubId)
                                         .map(clubsById::get)
                                         .filter(Objects::nonNull)
                                         .distinct()
                                         .toList();

        ListView<String> clubsList = new ListView<>();
        clubsList.getItems().addAll(clubs.stream()
                                        .map(SignUp::getClubId)
                                        .map(clubsById::get)
                                        .filter(Objects::nonNull)
                                        .map(Club::getName)
                                        .distinct()
                                        .toList());

        clubBox.getChildren().addAll(clubsLabel, clubsList);

        listsBox.getChildren().addAll(eventBox, clubBox);
        setCenter(listsBox);

        // Right side for details
        details.setWrapText(true);
        var right = new VBox(8, new Label("Details\n"), details);
        right.setPadding(new Insets(0,0,0,10));
        right.setPrefWidth(260);
        setRight(right);

        // When something is selected
        eventsList.getSelectionModel()
                  .selectedItemProperty()
                  .addListener((obs, oldTitle, newTitle) -> {
                      // Nothing selected
                      if (newTitle == null) {
                          if (clubsList.getSelectionModel().getSelectedItem() == null) {
                              details.setText(("Select an event or club for details."));
                          }
                          return;
                      }

                      clubsList.getSelectionModel().clearSelection();

                      Event ev = registeredEvents.stream()
                                                 .filter(e -> e.getTitle()
                                                                     .equals(newTitle))
                                                 .findFirst()
                                                 .orElse(null);

                      if (ev == null) {
                          details.setText("Select an event or club for details.");
                      } else {
                          details.setText(
                                  "Title: " + ev.getTitle() + "\n" +
                                  "Date: " + ev.getDateTime().format(df) + "\n" +
                                  "\n" + "Cost: " + String.format("$%.2f", ev.getCost()) + "\n" +
                                  "\n" + "Popularity: " + ev.getPopularity() + "\n" +
                                  "\n" + "Capacity: " + ev.getCapacity() + "\n" +
                                  "\n" + "ID: " + ev.getId()
                          );
                      }

                  });

        // When a club is selected, show club details and clear the event section
        clubsList.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldName, newName) -> {
                    if (newName == null) {
                        if (eventsList.getSelectionModel().getSelectedItem() == null) {
                            details.setText("Select an event or club to see details.");
                        }
                        return;
                    }

                    eventsList.getSelectionModel().clearSelection();

                    Club c = registeredClubs.stream()
                            .filter(cl -> cl.getName().equals(newName))
                            .findFirst()
                            .orElse(null);

                    if (c == null) {
                        details.setText("Select an event or club to see details.");
                    } else {
                        details.setText(
                                "Club: " + c.getName() + "\n" +
                                "\nDescription:\n" + c.getDescription() + "\n" +
                                "\nID: " + c.getId()
                        );
                    }
                });

        // Bottom for notifications
        notifications.setEditable(false);
        notifications.setPrefRowCount(5);
        notifications.setMaxWidth(Double.MAX_VALUE);

        VBox bottom = new VBox(10, new Label("Notifications\n"), notifications);
        bottom.setPadding(new Insets(10, 20, 20, 20));
        bottom.setFillWidth(true);

        setBottom(bottom);

    }

}
