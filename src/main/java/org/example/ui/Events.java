package org.example.ui;

import javafx.scene.layout.Priority;
import org.example.command.RegisterForEvent;
import org.example.observer.Observer;
import org.example.service.RegistrationService;
import org.example.strategy.CostSort;
import org.example.strategy.DateSort;
import org.example.strategy.PopularitySort;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import org.example.api.EventCatalogAPI;
import org.example.api.RegistrationAPI;
import org.example.domain.Event;
import org.example.domain.User;


public class Events extends BorderPane {

    private EventCatalogAPI catalogAPI;
    private RegistrationAPI registrationAPI;
    private final ObservableList<Event> model = FXCollections.observableArrayList();
    private final TextArea notifications = new TextArea();
    private final TextArea details = new TextArea("Select an event for details.");
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final RegistrationService rService;
    private final User user;
    private final ComboBox<String> categoryFilter = new ComboBox<>();

    public Events(EventCatalogAPI cAPI, RegistrationAPI rAPI, RegistrationService rs, User user) {
        this.catalogAPI = cAPI;
        this.registrationAPI = rAPI;
        this.rService = rs;
        this.user = user;

        setPadding(new Insets(12));

        // Sort selector at top
        var sortBox = new ComboBox<String>(FXCollections.observableArrayList("Date",
                                                                             "Popularity",
                                                                             "Cost"));

        // Category filter
        categoryFilter.setPromptText("All categories");

        var sortLabel = new Label("Sort by:");
        var catLabel = new Label("Category:");

        var topBar = new HBox(8, sortLabel, sortBox, catLabel, categoryFilter);
        topBar.setPadding(new Insets(0, 0, 10, 0));
        setTop(topBar);

        // Table of events set in center
        var table = new TableView<Event>(model);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        var cTitle = new TableColumn<Event, String>("Title");
        cTitle.setCellValueFactory(data ->
                                            new javafx.beans.property
                                                      .SimpleStringProperty(data.getValue()
                                                                            .getTitle()));

        var cDate = new TableColumn<Event, String>("Date");
        cDate.setCellValueFactory(data ->
                                            new javafx.beans.property
                                                      .SimpleStringProperty(data.getValue()
                                                                            .getDateTime().format(df)));

        var cCost = new TableColumn<Event, String>("Cost");
        cCost.setCellValueFactory(data ->
                                            new javafx.beans.property
                                                      .SimpleStringProperty(String
                                                                            .format("$%.2f",
                                                                                    data.getValue()
                                                                                    .getCost())));

        var cPop = new TableColumn<Event, String>("Popularity");
        cPop.setCellValueFactory(data ->
                                            new javafx.beans.property
                                                      .SimpleStringProperty(String
                                                                            .valueOf(data
                                                                                     .getValue()
                                                                                     .getPopularity())));

        var cCap = new TableColumn<Event, String>("Capacity");
        cCap.setCellValueFactory(data ->
                                            new javafx.beans.property
                                                      .SimpleStringProperty(String
                                                                            .valueOf(data
                                                                                     .getValue()
                                                                                     .getCapacity())));

        table.getColumns().addAll(cTitle, cDate, cCost, cPop, cCap);
        setCenter(table);

        // Details on the right side.
        details.setWrapText(true);
        details.setEditable(false);
        var right = new VBox(8, new Label("Details\n"), details);
        right.setPadding(new Insets(0,0,0,10));
        right.setPrefWidth(260);
        VBox.setVgrow(details, Priority.ALWAYS);

        HBox main = new HBox(8, table, right);
        main.setFillHeight(true);
        HBox.setHgrow(table, Priority.ALWAYS);

        setCenter(main);

        // Register button and notifications at the bottom.
        notifications.setEditable(false);
        notifications.setPrefRowCount(5);
        var registerBtn = new Button("Register Selected");
        registerBtn.setDisable(true);
        var bottom = new VBox(10,
                new HBox(8, registerBtn),
                new Label("Notifications"),
                notifications
        );
        bottom.setPadding(new Insets(10,0,0,0));
        setBottom(bottom);

        table.getSelectionModel().selectedItemProperty().addListener((obs, o, ev) -> {
            registerBtn.setDisable(ev == null);
            if (ev == null) {
                details.setText("Select an event to see details.");
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

        // Initial load and category list build, starting by loading all events one time to populate categories
        List<Event> allEvents = catalogAPI.listEvents();
        model.setAll(allEvents);

        // Build category list
        categoryFilter.getItems().add("All");
        allEvents.stream()
                 .map(Event::getCategory)
                 .filter(Objects::nonNull)
                 .map(String::trim)
                 .filter(s -> !s.isEmpty())
                 .distinct()
                 .sorted()
                 .forEach(categoryFilter.getItems()::add);
        categoryFilter.getSelectionModel().select("All");


        // Sorting behavior.
        Runnable reload = () -> {
            var key = sortBox.getSelectionModel().getSelectedItem();
            var category = categoryFilter.getSelectionModel().getSelectedItem();

            // Default for when nothing is selected at first
            if (key == null) {
                key = "Date";
                sortBox.getSelectionModel().select("Date");
            }

            // Default category
            if (category == null) {
                category = "All";
                categoryFilter.getSelectionModel().select("All");
            }

            switch (key) {
                case "Popularity" -> catalogAPI.setSortStrat(new PopularitySort());
                case "Cost" -> catalogAPI.setSortStrat(new CostSort());
                default -> catalogAPI.setSortStrat(new DateSort());
            }
            List<Event> events = catalogAPI.listEventsByCategory(category);
            model.setAll(events);
        };
        sortBox.setOnAction(e -> reload.run());
        categoryFilter.setOnAction(e -> reload.run());

        // First initial load
        reload.run();

        registerBtn.setOnAction(e -> {
            var ev = table.getSelectionModel().getSelectedItem();
            if (ev == null) return;
            // Replace with real session/user
            registrationAPI.submit(new RegisterForEvent(rs, ev.getId(), user.getId()));
        });

        // Show notifications.
        rs.attach((Observer) message -> {
            notifications.appendText(message + System.lineSeparator());
        });
    }

}
