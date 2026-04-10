package org.example.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import org.example.repository.InMemoryEventRepo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

public class CreateEvents extends BorderPane {

    public CreateEvents() {

        setPadding(new Insets(8));

        // Build UI for Event Form to allow user to create a new event and provide event details
        var eventForm = new VBox();

        var details = new Label("Enter the details of new event.");
        details.setPadding(new Insets(10));

        var eId = new HBox();
        var eIdLabel = new Label("Event ID: ");
        var eIdText = new TextField();
        eIdText.setText(UUID.randomUUID().toString());
        eId.setPadding(new Insets(10));
        eIdLabel.setPadding(new Insets(4));
        eId.setAlignment(Pos.CENTER_LEFT);
        eId.getChildren().addAll(eIdLabel, eIdText);

        var cId = new HBox();
        var cIdLabel = new Label("Club ID: ");
        var cIdText = new TextField();
        cId.setPadding(new Insets(10));
        cIdLabel.setPadding(new Insets(4));
        cId.setAlignment(Pos.CENTER_LEFT);
        cId.getChildren().addAll(cIdLabel, cIdText);

        var title = new HBox();
        var titleLabel = new Label("Title: ");
        var titleText = new TextField();
        title.setPadding(new Insets(10));
        titleLabel.setPadding(new Insets(4));
        title.setAlignment(Pos.CENTER_LEFT);
        title.getChildren().addAll(titleLabel, titleText);

        var date = new HBox();
        var dateLabel = new Label("Date (MM-DD-YYYY): ");
        var dateText = new TextField();
        date.setPadding(new Insets(10));
        dateLabel.setPadding(new Insets(4));
        date.setAlignment(Pos.CENTER_LEFT);
        date.getChildren().addAll(dateLabel, dateText);

        var time = new HBox();
        var timeLabel = new Label("Time (HH:MM[AM/PM]): ");
        var timeText = new TextField();
        time.setPadding(new Insets(10));
        timeLabel.setPadding(new Insets(4));
        time.setAlignment(Pos.CENTER_LEFT);
        time.getChildren().addAll(timeLabel, timeText);

        var popularity = new HBox();
        var popularityLabel = new Label("Popularity: ");
        var popularityText = new TextField();
        popularity.setPadding(new Insets(10));
        popularityLabel.setPadding(new Insets(4));
        popularity.setAlignment(Pos.CENTER_LEFT);
        popularity.getChildren().addAll(popularityLabel, popularityText);

        var capacity = new HBox();
        var capacityLabel = new Label("Capacity: ");
        var capacityText = new TextField();
        capacity.setPadding(new Insets(10));
        capacityLabel.setPadding(new Insets(4));
        capacity.setAlignment(Pos.CENTER_LEFT);
        capacity.getChildren().addAll(capacityLabel, capacityText);

        var cost = new HBox();
        var costLabel = new Label("Cost: ");
        var costText = new TextField();
        cost.setPadding(new Insets(10));
        costLabel.setPadding(new Insets(4));
        cost.setAlignment(Pos.CENTER_LEFT);
        cost.getChildren().addAll(costLabel, costText);

        var category = new HBox();
        var categoryLabel = new Label("Category: ");
        ObservableList<String> catOptions = FXCollections.observableArrayList(
                "music", "tech", "sports", "social", "career", "other"
        );
        var categories = new ComboBox<String>(catOptions);
        category.setPadding(new Insets(10));
        categoryLabel.setPadding(new Insets(4));
        category.setAlignment(Pos.CENTER_LEFT);
        category.getChildren().addAll(categoryLabel, categories);

        Button submitBtn = new Button("Submit");
        HBox bottom = new HBox(submitBtn);
        bottom.setAlignment(Pos.BASELINE_CENTER);
        bottom.setPadding(new Insets(40, 0, 0, 0));

        Label errMsg = new Label();
        errMsg.setVisible(false);
        HBox notification = new HBox(errMsg);
        notification.setAlignment(Pos.CENTER);
        notification.setPadding(new Insets(10, 0, 0, 0));

        // Handle submit button click
        submitBtn.setOnAction(e -> {
            // Retrieve all input from each of the form fields
            String eventId = eIdText.getText();
            String clubId = cIdText.getText();
            String eTitle = titleText.getText();
            String eDate = dateText.getText();
            String eTime = timeText.getText();
            int ePopularity = Integer.parseInt(popularityText.getText());
            int eCapacity = Integer.parseInt(capacityText.getText());
            int eCost = Integer.parseInt(costText.getText());
            String eCategory = categories.getSelectionModel().getSelectedItem();

            // Declare and initialize a registrant list
            JSONArray eRegistrants = new JSONArray();

            // Use if-else statement to determine if all form fields have been provided by the user
            if (eventId.isEmpty() || clubId.isEmpty() || eTitle.isEmpty() || eDate.isEmpty() || eTime.isEmpty() ||
                    popularityText.getText().isEmpty() || capacityText.getText().isEmpty() ||
                    costText.getText().isEmpty() || eCategory.isEmpty()) {
                errMsg.setText("Missing/invalid field entry. All fields are required!");
                errMsg.setVisible(true);
            } else {
                // If all fields have been provided, clear the form fields
                eIdText.clear();
                cIdText.clear();
                titleText.clear();
                dateText.clear();
                timeText.clear();
                popularityText.clear();
                capacityText.clear();
                costText.clear();

                // Create new event JSONObject to store event information
                JSONObject newEvent = new JSONObject();
                newEvent.put("id", eventId);
                newEvent.put("clubId", clubId);
                newEvent.put("title", eTitle);
                newEvent.put("date", eDate);
                newEvent.put("time", eTime);
                newEvent.put("status", "open");
                newEvent.put("capacity", eCapacity);
                newEvent.put("popularity", ePopularity);
                newEvent.put("category", eCategory);
                newEvent.put("cost", eCost);
                newEvent.put("registrants", eRegistrants);

                // Read the events.json file to gather existing events
                InMemoryEventRepo inMemoryEventRepo = new InMemoryEventRepo();
                JSONArray existingEvents = inMemoryEventRepo.readEventsJSONFile();

                // Add new event to the events list
                existingEvents.put(newEvent);
                // Write JSONArray that is storing all existing events back to the events.json file
                inMemoryEventRepo.writeEventsJSONFile(existingEvents);

                // Notify the user that the new event has been successfully added
                errMsg.setText("New event successfully added!");
                errMsg.setVisible(true);
            }
        });

        // Generate form
        eventForm.getChildren().addAll(details, eId, cId, title, date, time, popularity, capacity, cost, category, bottom, notification);
        setCenter(eventForm);
    }

}
