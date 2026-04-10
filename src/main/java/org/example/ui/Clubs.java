package org.example.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.api.ClubActivityAPI;
import org.example.api.ClubCatalogAPI;
import org.example.api.EventCatalogAPI;
import org.example.api.SignUpAPI;
import org.example.command.ClubSignUp;
import org.example.domain.Club;
import org.example.domain.Role;
import org.example.domain.User;
import org.example.repository.InMemoryClubRepo;
import org.example.service.SignUpService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Optional;

public class Clubs extends BorderPane {

    private final ClubCatalogAPI clubCatAPI;
    private final SignUpAPI signUpAPI;
    private final ObservableList<Club> model = FXCollections.observableArrayList();
    private final TextArea notifications = new TextArea();
    private final TextArea details = new TextArea("Select a club for details.");
    private final SignUpService sService;
    private final User user;
    private final EventCatalogAPI eventCatalogAPI;
    private final ClubActivityAPI clubActivityAPI;

    public Clubs(ClubCatalogAPI clubCatAPI,
                 SignUpAPI signUpAPI,
                 SignUpService ss,
                 EventCatalogAPI ecAPI,
                 ClubActivityAPI caAPI,
                 User user) {
        this.clubCatAPI = clubCatAPI;
        this.signUpAPI = signUpAPI;
        this.sService = ss;
        this.user = user;
        this.eventCatalogAPI = ecAPI;
        this.clubActivityAPI = caAPI;

        setPadding(new Insets(8));

        boolean canPost = (user.getRole() == Role.CLUB_LEADER || user.getRole() == Role.ADMIN);

        // Center the table
        TableView<Club> table = new TableView<>(model);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No Clubs currently available."));

        TableColumn<Club, String> cName = new TableColumn<>("Name");
        cName.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getName())
        );
        cName.setStyle("-fx-alignment: CENTER;");

        table.getColumns().addAll(cName);
        setCenter(table);

        // Right side for details
        details.setWrapText(true);
        details.setEditable(false);
        VBox right = new VBox(8, new Label("\nDetails\n"), details);
        right.setPadding(new Insets(0, 0, 0, 10));
        right.setPrefWidth(260);
        VBox.setVgrow(details, Priority.ALWAYS);

        TextArea announcementInput = new TextArea();
        announcementInput.setPromptText("Write an announcement for this club...");
        announcementInput.setPrefRowCount(2);

        Button postAnnouncementBtn = new Button("Post Announcement");
        Button manageAnnouncementBtn = new Button("Manage");

        TextArea eventUpdateInput = new TextArea();
        eventUpdateInput.setPromptText("Write an event update for this club (e.g., 'Time changed to 6:00PM')");
        eventUpdateInput.setPrefRowCount(2);

        Button postEventBtn = new Button("Post Event Update");
        Button manageEventBtn = new Button("Manage");

        VBox postBox = new VBox();

        if (canPost) {
            HBox aButtons = new HBox(8, postAnnouncementBtn, manageAnnouncementBtn);
            HBox eButtons = new HBox(8, postEventBtn, manageEventBtn);

            postBox = new VBox(6,
                    new Label("Club Leader Tools:"),
                    new Label("Announcement:"),
                    announcementInput,
                    aButtons,
                    new Label("Event Update:"),
                    eventUpdateInput,
                    eButtons
            );
            postBox.setPadding(new Insets(10, 0, 0, 0));
            right.getChildren().add(postBox);

            manageAnnouncementBtn.setDisable(true);
            manageEventBtn.setDisable(true);
        }

        HBox main = new HBox(8, table, right);
        main.setFillHeight(true);
        HBox.setHgrow(table, Priority.ALWAYS);

        setCenter(main);

        // Sign up button at bottom
        notifications.setEditable(false);
        notifications.setPrefRowCount(5);
        Button signUpBtn = new Button("Sign Up for Club");
        var bottom = new VBox(10,
                new HBox(8, signUpBtn),
                new Label("Notifications"),
                notifications
        );
        bottom.setPadding(new Insets(10,0,0,0));
        setBottom(bottom);

        VBox finalPostBox = postBox;
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldClub, club) -> {
            if (club == null) {
                details.setText("Select a club.");
                return;
            }

            String clubId = club.getId();

            StringBuilder sb = new StringBuilder();

            // Basic club info
            sb.append("Name: ").append(club.getName()).append("\n\n")
                    .append("Description: ").append(club.getDescription()).append("\n\n")
                    .append("ID: ").append(clubId).append("\n\n");

            // Events from clubActivity.json
            sb.append("Events:\n");
            var events = clubActivityAPI.listEventsForClub(clubId);
            if (events.isEmpty()) {
                sb.append("No events yet.\n");
            } else {
                for (String ev : events) {
                    sb.append("  - ").append(ev).append("\n");
                }
            }

            // Announcements from clubActivity.json
            sb.append("\nAnnouncements:\n");
            var announcements = clubActivityAPI.listAnnouncementsForClub(clubId);
            if (announcements.isEmpty()) {
                sb.append("No announcements yet.\n");
            } else {
                for (String a : announcements) {
                    sb.append("  - ").append(a).append("\n");
                }
            }

            details.setText(sb.toString());

            InMemoryClubRepo clubRepo = new InMemoryClubRepo();
            JSONArray getClubs = clubRepo.readClubs();
            boolean permission = false;

            if (user.getRole() == Role.ADMIN) {
                permission = true;
            }

            for (int i = 0; i < getClubs.length(); i++) {
                JSONObject c = getClubs.getJSONObject(i);

                if (c.getString("id").equals(clubId)) {
                    JSONArray clubLeaders = c.getJSONArray("clubLeader");

                    for (int j = 0; j < clubLeaders.length(); j++) {
                        String cL = clubLeaders.getString(j);

                        if (cL.equals(user.getId())) {
                            permission = true;
                            break;
                        }
                    }
                }

            }

            if (permission) {
                manageAnnouncementBtn.setDisable(false);
                manageEventBtn.setDisable(false);
                finalPostBox.setVisible(true);

                postAnnouncementBtn.setOnAction(e -> {
                    String text = announcementInput.getText().trim();
                    if (text.isEmpty()) return;

                    clubActivityAPI.postAnnouncementForClub(clubId, text);
                    announcementInput.clear();

                    sService.notifyObservers("Announcement posted for Club " + club.getName());

                    table.getSelectionModel().select(club);
                });

                postEventBtn.setOnAction(e -> {
                    String text = eventUpdateInput.getText().trim();
                    if (text.isEmpty()) return;

                    clubActivityAPI.postEventUpdateForClub(clubId, text);
                    eventUpdateInput.clear();

                    sService.notifyObservers("Event update for Club " + club.getName());

                    table.getSelectionModel().select(club);
                });

                manageAnnouncementBtn.setOnAction(e -> {
                    var currentClub = table.getSelectionModel().getSelectedItem();
                    if (currentClub == null) return;

                    String cid = currentClub.getId();
                    var annList = clubActivityAPI.listAnnouncementsForClub(cid);
                    if (annList.isEmpty()) {
                        new Alert(Alert.AlertType.INFORMATION, "No announcements to manage for this club.").showAndWait();
                        return;
                    }

                    ChoiceDialog<String> dialog = new ChoiceDialog<>(annList.get(0), annList);
                    dialog.setTitle("Manage Announcements");
                    dialog.setHeaderText("Select an announcement to delete");
                    dialog.setContentText("Announcement:");

                    Optional<String> result = dialog.showAndWait();
                    result.ifPresent(selected -> {
                        clubActivityAPI.deleteAnnouncementForClub(cid, selected);
                        sService.notifyObservers("Announcement deleted for " + currentClub.getName());
                        table.getSelectionModel().select(currentClub);
                    });
                });

                manageEventBtn.setOnAction(e -> {
                    var currentClub = table.getSelectionModel().getSelectedItem();
                    if (currentClub == null) return;

                    String cid = currentClub.getId();
                    var evList = clubActivityAPI.listEventsForClub(cid);
                    if (evList.isEmpty()) {
                        new Alert(Alert.AlertType.INFORMATION, "No event entries to manage for this club.").showAndWait();
                        return;
                    }

                    ChoiceDialog<String> dialog = new ChoiceDialog<>(evList.get(0), evList);
                    dialog.setTitle("Manage Event Entries");
                    dialog.setHeaderText("Select an event entry to delete");
                    dialog.setContentText("Event entry:");

                    Optional<String> result = dialog.showAndWait();
                    result.ifPresent(selected -> {
                        clubActivityAPI.deleteEventEntryForClub(cid, selected);
                        sService.notifyObservers("Event entry deleted for " + currentClub.getName());
                        table.getSelectionModel().select(currentClub);
                    });
                });
            } else {
                finalPostBox.setVisible(false);
            }
        });

        reload();

        signUpBtn.setOnAction(e -> {
            var cl = table.getSelectionModel().getSelectedItem();
            if (cl == null) return;

            signUpAPI.submit(new ClubSignUp(ss, cl.getId(), user.getId()));
        });

        // Show notifications.
        ss.attach(message -> {
            notifications.appendText(message + System.lineSeparator());
        });
    }

    private void reload() {
        List<Club> clubs = clubCatAPI.listClubs();
        model.setAll(clubs);
    }

}
