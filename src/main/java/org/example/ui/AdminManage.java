package org.example.ui;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.api.ClubCatalogAPI;
import org.example.api.EventCatalogAPI;
import org.example.api.RegistrationAPI;
import org.example.api.SignUpAPI;
import org.example.domain.User;
import org.example.observer.NotificationService;
import org.example.repository.InMemoryClubRepo;
import org.example.repository.InMemoryEventRepo;
import org.example.repository.InMemoryRegistrationRepo;
import org.example.repository.InMemorySignUpRepo;
import org.example.repository.InMemoryUserRepo;
import org.example.repository.RegistrationRepo;
import org.example.repository.SignUpRepo;
import org.example.service.ClubCatalogService;
import org.example.service.EventCatalogService;
import org.example.service.RegistrationService;
import org.example.service.SeedJson;
import org.example.service.SignUpService;
import org.example.strategy.DateSort;
import org.example.ui.admin_tableconfig.*;
import org.example.ui.helpers.AdminAnnouncementHelper;
import org.example.ui.helpers.AdminClubHelper;
import org.example.ui.helpers.AdminEventHelper;
import org.example.ui.helpers.AdminUserHelper;

import static org.example.ui.admin_tableconfig.AnnouncementsConfig.loadAnnouncements;
import static org.example.ui.admin_tableconfig.ClubsConfig.loadClubs;
import static org.example.ui.admin_tableconfig.EventsConfig.loadEvents;


public class AdminManage extends BorderPane {

    private final User user;
    private final String adminUserId;
    private final ComboBox<String> typeChoice = new ComboBox<>();

    // Tables and row lists
    private final TableView<ClubRow> cTable = new TableView<>();
    private final TableView<EventRow> eTable = new TableView<>();
    private final TableView<AnnouncementRow> aTable = new TableView<>();

    private final ObservableList<ClubRow> cRows = FXCollections.observableArrayList();
    private final ObservableList<EventRow> eRows = FXCollections.observableArrayList();
    private final ObservableList<AnnouncementRow> aRows = FXCollections.observableArrayList();

    // Actions
    private final Button deleteBtn = new Button("Delete");
    private final Button manageMemsBtn = new Button("Manage Members");

    // Details and notifications
    private final TextArea details = new TextArea();
    private final TextArea notifications = new TextArea();

    // Repositories for data access
    private final InMemoryEventRepo eRepo;
    private final InMemoryClubRepo cRepo;
    private final RegistrationRepo regRepo;
    private final SignUpRepo sRepo;
    private final InMemoryUserRepo uRepo;

    // Services to implement core behavior and be subjects to Observer pattern
    private final ClubCatalogService clubCatalogService;
    private final EventCatalogService eventCatalogService;
    private final RegistrationService regService;
    private final SignUpService signUpService;

    // APIs to be called when issuing commands, rather than calling services directly
    private final ClubCatalogAPI clubCatalogAPI;
    private final EventCatalogAPI eventCatalogAPI;
    private final RegistrationAPI registrationAPI;
    private final SignUpAPI signUpAPI;

    // Observer for notifications
    private final NotificationService notificationService;

    // Helpers
    private final AdminUserHelper userHelper;
    private final AdminClubHelper clubHelper;
    private final AdminEventHelper eventHelper;
    private final AdminAnnouncementHelper announcementHelper;



    public AdminManage(User user, String adminUserId) {
        this.user = user;
        this.adminUserId = adminUserId;

        // Construct repos
        this.eRepo = new InMemoryEventRepo();
        this.cRepo = new InMemoryClubRepo();
        this.regRepo = new InMemoryRegistrationRepo();
        this.sRepo = new InMemorySignUpRepo();
        this.uRepo = new InMemoryUserRepo();

        // Seed in memory repos so services can query them
        SeedJson.seedEvents(eRepo);
        SeedJson.seedClubs(cRepo);

        // Construct services
        this.eventCatalogService = new EventCatalogService(eRepo, new DateSort());
        this.clubCatalogService = new ClubCatalogService(cRepo);
        this.regService = new RegistrationService(regRepo, eRepo);
        this.signUpService = new SignUpService(sRepo, cRepo);

        // Construct APIs
        this.eventCatalogAPI = new EventCatalogAPI(eventCatalogService);
        this.clubCatalogAPI = new ClubCatalogAPI(clubCatalogService);
        this.registrationAPI = new RegistrationAPI();
        this.signUpAPI = new SignUpAPI();

        // Observer pattern wiring
        this.notificationService = new NotificationService(adminUserId);

        // Attach observer to subjects that should make notifications
        this.eventCatalogService.attach(notificationService);
        this.regService.attach(notificationService);
        this.signUpService.attach(notificationService);

        // Helpers
        this.userHelper = new AdminUserHelper(uRepo);
        this.clubHelper = new AdminClubHelper(cRepo, userHelper, details, notifications, cRows, user, adminUserId);
        this.eventHelper = new AdminEventHelper(eRepo, userHelper, details, notifications, eRows, user, adminUserId);
        this.announcementHelper = new AdminAnnouncementHelper(cRepo, details, notifications, aRows, user, adminUserId);

        // UI setup, starting with the top
        setPadding(new Insets(12));

        Label title = new Label("Admin - Mange Clubs, Events, and Announcements");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold");

        Label choice = new Label("Choose what to manage");
        typeChoice.getItems().addAll("Clubs",  "Events", "Announcements");
        typeChoice.setPromptText("Select");

        HBox topBar = new HBox(12, choice, typeChoice);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox top = new VBox(12, title, topBar);
        top.setPadding(new Insets(8, 8, 12, 8));
        setTop(top);

        // Details for the right side
        details.setWrapText(true);
        details.setEditable(false);

        Label dLabel = new Label("Details");
        VBox rBox = new VBox(6, dLabel, details);
        rBox.setPadding(new Insets(8));
        rBox.setAlignment(Pos.CENTER_LEFT);
        rBox.setPrefWidth(260);
        VBox.setVgrow(details, Priority.ALWAYS);
        setRight(rBox);

        // Bottom notifications
        notifications.setEditable(false);
        notifications.setWrapText(true);
        notifications.setPrefRowCount(4);

        deleteBtn.setDisable(true);
        manageMemsBtn.setDisable(true);

        HBox actions = new HBox(10, deleteBtn, manageMemsBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox bottom = new VBox(6, new Separator(), actions,
                               new Label("Notifications"), notifications);
        bottom.setPadding(new Insets(8));
        setBottom(bottom);

        // Selection behavior
        typeChoice.valueProperty().addListener((obs,
                                                oldVal,
                                                newVal) -> {
            if (newVal == null) {
                setCenter(null);
                deleteBtn.setDisable(true);
                manageMemsBtn.setDisable(true);
                details.clear();
                return;
            }

            switch (newVal) {
                case "Clubs" -> {
                   if (cTable.getColumns().isEmpty()) {
                       ClubsConfig.configure(cTable, cRows);
                   }
                   setCenter(cTable);
                   loadClubs(cRepo,  cRows, user, adminUserId);
                   deleteBtn.setText("Delete Club");
                   manageMemsBtn.setDisable(false);
                    manageMemsBtn.setText("Manage Members");
                   details.setText("Select a club to see its details.");
                }
                case "Events" -> {
                    if (eTable.getColumns().isEmpty()) {
                        EventsConfig.configure(eTable, eRows);
                    }
                    setCenter(eTable);
                    loadEvents(eRepo,  eRows, user, adminUserId);
                    deleteBtn.setText("Delete Event");
                    manageMemsBtn.setDisable(false);
                    manageMemsBtn.setText("Manage Registrants");
                    details.setText("Select an event to see its details.");
                }
                case "Announcements" -> {
                    if (aTable.getColumns().isEmpty()) {
                        AnnouncementsConfig.configure(aTable, aRows);
                    }
                    setCenter(aTable);
                    loadAnnouncements(cRepo,  aRows, user, adminUserId);
                    deleteBtn.setText("Delete Announcement");
                    manageMemsBtn.setDisable(true);
                    details.setText("Select an announcement to see its details.");
                }
            }
        });

        // Initial selection
        typeChoice.setValue("Clubs");


        // Row selection actions, starting with clubs
        cTable.getSelectionModel().selectedItemProperty().addListener((obs,
                                                                       old,
                                                                       row) -> {
            if (!"Clubs".equals(typeChoice.getValue())) {
                return;
            }

            if (row == null) {
                deleteBtn.setDisable(true);
                manageMemsBtn.setDisable(true);
                details.clear();
            } else {
                deleteBtn.setDisable(false);
                manageMemsBtn.setDisable(false);
                clubHelper.showClubDetails(row);
            }
        });

        // Row selection actions for events
        eTable.getSelectionModel().selectedItemProperty().addListener((obs,
                                                                       old,
                                                                       row) -> {
            if (!"Events".equals(typeChoice.getValue())) {
                return;
            }
            if (row == null) {
                deleteBtn.setDisable(true);
                details.clear();
            } else {
                deleteBtn.setDisable(false);
                eventHelper.showEventDetails(row);
            }
        });

        // Row selection actions for announcements
        aTable.getSelectionModel().selectedItemProperty().addListener((obs,
                                                                       old,
                                                                       row) -> {
            if (!"Announcements".equals(typeChoice.getValue())) {
                return;
            }
            if (row == null) {
                deleteBtn.setDisable(true);
                details.clear();
            } else {
                deleteBtn.setDisable(false);
                announcementHelper.showAnnouncementDetails(row);
            }
        });

        // Button actions
        deleteBtn.setOnAction(e -> {
            String selected = typeChoice.getValue();
            if (selected == null) {
                return;
            }
            switch (selected) {
                case "Clubs" -> {
                    ClubRow row = cTable.getSelectionModel().getSelectedItem();
                    clubHelper.deleteClub(row);
                }
                case "Events" -> {
                    EventRow row = eTable.getSelectionModel().getSelectedItem();
                    eventHelper.deleteEvent(row);
                }
                case "Announcements" -> {
                    AnnouncementRow row = aTable.getSelectionModel().getSelectedItem();
                    announcementHelper.deleteAnnouncement(row);
                }
            }
            // Disable buttons until a new row is selected
            deleteBtn.setDisable(true);
            manageMemsBtn.setDisable(true);
        });

        manageMemsBtn.setOnAction(e -> {
            String selectedType = typeChoice.getValue();
            if ("Clubs".equals(selectedType)) {
                ClubRow clubRow = cTable.getSelectionModel().getSelectedItem();
                if (clubRow != null) {
                    clubHelper.manageMembers(clubRow);
                }
            } else if ("Events".equals(selectedType)) {
                EventRow eventRow = eTable.getSelectionModel().getSelectedItem();
                if (eventRow != null) {
                    eventHelper.manageRegistrants(eventRow);
                }
            }
        });

    }

}
