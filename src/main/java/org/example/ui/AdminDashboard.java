package org.example.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminDashboard extends BorderPane {

    private final ObservableList<ActivityRow> activities;
    private final Map<String, ClubInfo> clubsById;
    private final Map<String, EventInfo> eventsById;
    private final String adminUserId;
    private final TextArea notifications = new TextArea();
    private final TextArea details = new TextArea();

    // Attempting to pull date and time from club activities
    private static final Pattern ACTIVITY_DATE_TIME_PATTERN =
            Pattern.compile(".* on (\\d{2}-\\d{2}-\\d{4})(?: at (\\d{1,2}:\\d{2}[AP]M))?.*",
                            Pattern.CASE_INSENSITIVE);

    public AdminDashboard(String adminId) {
        this.adminUserId = adminId;

        setPadding(new Insets(12));

        Label title = new Label("Admin Dashboard");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        setTop(title);
        BorderPane.setMargin(title, new Insets(0, 0, 10, 0));

        // Build rows for everything
        List<ActivityRow> rows = new ArrayList<>();

        Map<String, EventInfo> eMap = new HashMap<>();
        // add club and event rows
        Map<String, ClubInfo> cMap = loadById(rows, eMap);
        this.eventsById = eMap;
        this.clubsById = cMap;

        // Add activity rows
        rows.addAll(loadActivitiesFromJSON());

        this.activities = FXCollections.observableArrayList(rows);

        // Center the table
        TableView<ActivityRow> table = new TableView<>(activities);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No clubs, events, or activities to display."));

        TableColumn<ActivityRow, String> ceCol = new TableColumn<>("Clubs, Events, and Activities");
        ceCol.setCellValueFactory(c -> c.getValue().clubNameProperty());

        TableColumn<ActivityRow, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c -> c.getValue().typeProperty());

        TableColumn<ActivityRow, String> detailsCol = new TableColumn<>("Date and Time");
        detailsCol.setCellValueFactory(c -> c.getValue().dateTimeProperty());

        table.getColumns().addAll(ceCol, typeCol, detailsCol);
        setCenter(table);

        // Right side for details
        details.setWrapText(true);
        details.setEditable(false);

        Label dLabel = new Label("Details");
        VBox right = new VBox(8, dLabel, details);
        right.setPadding(new Insets(0, 0, 0, 10));
        right.setPrefWidth(260);

        // Let details box grow to fill VBox height
        VBox.setVgrow(details, Priority.ALWAYS);

        HBox cBox = new HBox(8, table, right);
        cBox.setFillHeight(true);

        // Table expands horizontally with window
        HBox.setHgrow(table, Priority.ALWAYS);

        setCenter(cBox);

        // For flagging and notifications area
        notifications.setEditable(false);
        notifications.setPrefRowCount(5);

        Button flagBtn = new Button("Flag Selected");
        flagBtn.setDisable(true);

        // Notification box at the bottom
        VBox bottom = new VBox(10,
                new HBox(8, flagBtn),
                new Label("Notifications"),
                notifications
        );
        bottom.setPadding(new Insets(10, 0, 0, 0));
        setBottom(bottom);

        // Handle selections
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldRow, newRow) -> {
            flagBtn.setDisable(newRow == null);
            if (newRow == null) {
                details.setText("Select a row to see details.");
            } else {
                details.setText(buildDetailsText(newRow));
            }
        });

        flagBtn.setOnAction(e -> {
            ActivityRow row = table.getSelectionModel().getSelectedItem();
            if (row == null) return;
            flagActivity(row);
            notifications.appendText("Flagged " + row.getType() +
                    " (" + row.getClubName() + ")\n");
        });

        details.setText("Select a row to see details.");
    }

    // Load clubs and events, populate rows and id maps
    private Map<String, ClubInfo> loadById(List<ActivityRow> rows,
                                           Map<String, EventInfo> eventsById) {
        Map<String, ClubInfo> clubMap = new HashMap<>();

        // For clubs
        File cFile = new File("src/main/resources/clubs.json");
        if (cFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(cFile))) {
                String json = reader.readLine();
                if (json != null && !json.isEmpty()) {
                    JSONArray arr = new JSONArray(json);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        String id = obj.getString("id");
                        String name = obj.getString("name");
                        String description = obj.optString("description", "");

                        ClubInfo clubInfo = new ClubInfo(id, name, description);
                        clubMap.put(id, clubInfo);

                        String rowId = "club-" + id;
                        rows.add(new ActivityRow(
                                rowId,
                                id,
                                clubInfo.name(),
                                "club",
                                clubInfo.description(),
                                ""  // Since clubs do not have a date or time for us
                        ));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Error reading clubs.json", e);
            }
        }

        // For events
        File eFile = new File("src/main/resources/events.json");
        if (eFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(eFile))) {
                String json = reader.readLine();
                if (json != null && !json.isEmpty()) {
                    JSONArray eventsArray = new JSONArray(json);
                    for (int i = 0; i < eventsArray.length(); i++) {
                        JSONObject evt = eventsArray.getJSONObject(i);

                        String eventId = evt.optString("id", "event-" + i);
                        String clubId = evt.optString("clubId", "");
                        String title = evt.optString("tile", ""); // key is tile in JSON, I figure we will correct this at some point I just am keeping it uniform for the moment
                        String date = evt.optString("date", "");
                        String time = evt.optString("time", "");
                        String status = evt.optString("status", "");
                        int popularity = evt.optInt("popularity", 0);
                        int capacity = evt.optInt("capacity", 0);
                        double cost = 0.0;  // This will need to be added into the JSON file if we want to include it! Hardcoded for now while we decide.

                        EventInfo eventInfo = new EventInfo(
                                eventId, clubId, title, date, time, status, popularity, capacity, cost
                        );
                        eventsById.put(eventId, eventInfo);

                        // rawDetails for right side and for logging
                        String rawDetails = buildDetails(title, date, time, status);
                        String dateTime = combineDateTime(date, time);

                        rows.add(new ActivityRow(
                                eventId,
                                clubId,
                                title,
                                "event",
                                rawDetails,
                                dateTime
                        ));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Error reading events.json", e);
            }
        }

        return clubMap;
    }

    // Load activities
    private List<ActivityRow> loadActivitiesFromJSON() {
        List<ActivityRow> rows = new ArrayList<>();
        File file = new File("src/main/resources/clubActivity.json");

        if (!file.exists()) {
            return rows;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String json = reader.readLine();
            if (json == null || json.isEmpty()) {
                return rows;
            }

            JSONArray clubsArray = new JSONArray(json);
            for (int i = 0; i < clubsArray.length(); i++) {
                JSONObject clubObj = clubsArray.getJSONObject(i);

                String clubId = clubObj.optString("id", "");
                ClubInfo clubInfo = clubsById.getOrDefault(
                        clubId,
                        new ClubInfo(clubId, "Unknown Club (" + clubId + ")", "")
                );

                JSONArray activitiesArray = clubObj.optJSONArray("activity");
                if (activitiesArray == null) {
                    continue;
                }

                for (int j = 0; j < activitiesArray.length(); j++) {
                    JSONObject actObj = activitiesArray.getJSONObject(j);

                    String type = actObj.optString("type", "");
                    String detailsText = actObj.optString("details", "");

                    String activityId = clubId + "-" + j;

                    String dateTime = extractDateTime(detailsText);

                    rows.add(new ActivityRow(
                            activityId,
                            clubId,
                            clubInfo.name(),
                            type,
                            detailsText,
                            dateTime
                    ));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading clubActivity.json", e);
        }

        return rows;
    }

    // This is for building the details based on if we are selecting events or not. If selecting events, details will show popularity, capacity and cost.
    private String buildDetails(String title, String date, String time, String status) {
        StringBuilder rawDetails = new StringBuilder();
        if (!title.isEmpty()) {
            rawDetails.append(title);
        }
        if (!date.isEmpty()) {
            if (!rawDetails.isEmpty()) rawDetails.append(" on ");
            rawDetails.append(date);
        }
        if (!time.isEmpty()) {
            if (!rawDetails.isEmpty()) rawDetails.append(" at ");
            rawDetails.append(time);
        }
        if (!status.isEmpty()) {
            if (!rawDetails.isEmpty()) rawDetails.append(" (").append(status).append(")");
        }
        return rawDetails.toString();
    }

    // Combine the date and time
    private String combineDateTime(String date, String time) {
        if ((date == null || date.isEmpty()) && (time == null || time.isEmpty())) {
            return "";
        }
        if (date == null || date.isEmpty()) {
            return time;
        }
        if (time == null || time.isEmpty()) {
            return date;
        }

        return date + " -- " + time;
    }

    // Pull date and time from details text if present
    private String extractDateTime(String text) {
        Matcher m = ACTIVITY_DATE_TIME_PATTERN.matcher(text);
        if (m.matches()) {
            String date = m.group(1);
            String time = m.group(2);
            return (time == null || time.isEmpty()) ? date : date + " " + time;
        }
        return "";
    }

    // Details text
    private String buildDetailsText(ActivityRow row) {

        // IF the row is an event, show all of its associated details
        if("event".equalsIgnoreCase(row.getType())) {
            EventInfo eInfo = eventsById.get(row.getId());
            if (eInfo != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Title: ").append(eInfo.title()).append("\n");

                if (eInfo.date() != null && !eInfo.date().isEmpty()) {
                    sb.append("Date: ").append(eInfo.date()).append("\n");
                }
                if (eInfo.time() != null && !eInfo.time().isEmpty()) {
                    sb.append("Time: ").append(eInfo.time()).append("\n");
                }

                // Show details still
                if (row.getDetails() != null && !row.getDetails().isEmpty()) {
                    sb.append("\nDetails: ").append(row.getDetails()).append("\n");
                }

                // Hard-coded cost for now until we add it into JSON
                sb.append("\nCost: $").append(String.format("%.2f", eInfo.cost())).append("\n");
                sb.append("\nPopularity: ").append(eInfo.popularity()).append("\n");
                sb.append("\nCapacity: ").append(eInfo.capacity()).append("\n");
                sb.append("\nID: ").append(eInfo.id());

                return sb.toString();
            }
        }

        // Generic default
        StringBuilder sb = new StringBuilder();
        sb.append("Type: ").append(row.getType()).append("\n")
                .append("Label: ").append(row.getClubName()).append("\n")
                .append("\nDetails:\n").append(row.getDetails()).append("\n")
                .append("\nID: ").append(row.getId()).append("\n")
                .append("Club ID: ").append(row.getClubId());

        return sb.toString();
    }

    // Log a flagged row
    private void flagActivity(ActivityRow row) {
        File file = new File("src/main/resources/administrativeLog.json");
        JSONArray logArray;

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                if (line == null || line.isEmpty()) {
                    logArray = new JSONArray();
                } else {
                    logArray = new JSONArray(line);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error reading administrativeLog.json", e);
            }
        } else {
            logArray = new JSONArray();
        }

        JSONObject entry = new JSONObject();
        entry.put("activityId", row.getId());
        entry.put("clubId", row.getClubId());
        entry.put("name", row.getClubName());
        entry.put("type", row.getType());
        entry.put("details", row.getDetails());
        entry.put("flaggedBy", adminUserId != null ? adminUserId : "unknown");
        entry.put("flaggedAt", Instant.now().toString());

        logArray.put(entry);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write(logArray.toString());
        } catch (IOException e) {
            throw new RuntimeException("Error writing administrativeLog.json", e);
        }
    }

    // Data holders
    private record ClubInfo(String id, String name, String description) { }

    private record EventInfo(String id, String clubId, String title,
                             String date, String time, String status,
                             int popularity, int capacity, double cost) { }

    // One row in the Admin table
    public static class ActivityRow {
        private final String id;
        private final String clubId;
        private final SimpleStringProperty clubName;
        private final SimpleStringProperty type;
        private final SimpleStringProperty details;
        private final SimpleStringProperty dateTime;

        public ActivityRow(String id, String clubId, String clubName,
                           String type, String details, String dateTime) {
            this.id = id;
            this.clubId = clubId;
            this.clubName = new SimpleStringProperty(clubName);
            this.type = new SimpleStringProperty(type);
            this.details = new SimpleStringProperty(details);
            this.dateTime = new SimpleStringProperty(dateTime);
        }

        public String getId()       { return id; }
        public String getClubId()   { return clubId; }
        public String getClubName() { return clubName.get(); }
        public String getType()     { return type.get(); }
        public String getDetails()  { return details.get(); }

        public SimpleStringProperty clubNameProperty() { return clubName; }
        public SimpleStringProperty typeProperty()     { return type; }
        public SimpleStringProperty dateTimeProperty() { return dateTime; }
    }
}

