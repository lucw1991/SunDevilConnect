package org.example.ui.admin_tableconfig;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.example.domain.Role;
import org.example.domain.User;
import org.example.repository.InMemoryClubRepo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnouncementsConfig {

    private AnnouncementsConfig() {}

    public static void configure(TableView<AnnouncementRow> table, ObservableList<AnnouncementRow> rows) {

        table.setItems(rows);

        TableColumn<AnnouncementRow, String> colClub = new TableColumn<>("Club");
        colClub.setCellValueFactory(c -> c.getValue().clubIdProperty());
        colClub.setPrefWidth(180);

        TableColumn<AnnouncementRow, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(c -> c.getValue().typeProperty());
        colType.setPrefWidth(90);

        TableColumn<AnnouncementRow, String> colSummary = new TableColumn<>("Summary");
        colSummary.setCellValueFactory(c -> c.getValue().summaryProperty());
        colSummary.setPrefWidth(260);

        table.getColumns().setAll(colClub, colType, colSummary);

    }

    // Load from JSON
    public static void loadAnnouncements(InMemoryClubRepo clubRepo,
                                         ObservableList<AnnouncementRow> rows, User user, String adminUserId) {

        rows.clear();

        // Map the clubId to the clubName
        JSONArray cArray = clubRepo.readClubs();
        Map<String, String> cNames = new HashMap<>();
        for (int i = 0; i < cArray.length(); i++) {
            JSONObject obj = cArray.getJSONObject(i);
            String id = obj.optString("id", "");
            String name = obj.optString("name", "Club " + id);
            cNames.put(id, name);
        }

        File file = new File("src/main/resources/clubActivity.json");
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String json = reader.readLine();
            if (json == null || json.isEmpty()) {
                return;
            }

            // Determine clubs user is a leader of
            JSONArray clubs = clubRepo.readClubs();

            List<String> clubLeaderClubs = new ArrayList<>();

            if (user.getRole() == Role.ADMIN) {
                for (int i = 0; i < clubs.length(); i++) {
                    JSONObject club = clubs.getJSONObject(i);
                    clubLeaderClubs.add(club.getString("id"));
                }
            } else {
                for (int i = 0; i < clubs.length(); i++) {
                    JSONObject club = clubs.getJSONObject(i);
                    JSONArray clubLeaders = club.getJSONArray("clubLeader");

                    for (int j = 0; j < clubLeaders.length(); j++) {
                        String clubLeaderId = clubLeaders.getString(j);

                        if (clubLeaderId.equals(adminUserId)) {
                            clubLeaderClubs.add(club.getString("id"));
                            break;
                        }
                    }
                }
            }

            for (String clubId : clubLeaderClubs) {
                JSONArray activityArray = new JSONArray(json);
                for (int i = 0; i < activityArray.length(); i++) {
                    JSONObject obj = activityArray.getJSONObject(i);
                    String cId = obj.optString("id", "");
                    String cName = cNames.getOrDefault(cId, "Club " + cId);

                    if (clubId.equals(cId)) {
                        JSONArray activities = obj.optJSONArray("activity");
                        if (activities == null) {
                            continue;
                        }

                        for (int j = 0; j < activities.length(); j++) {
                            JSONObject activity = activities.getJSONObject(j);
                            String type = activity.optString("type", "");
                            String details = activity.optString("details", "");

                            if (!"announcement".equalsIgnoreCase(type)) {
                                continue;
                            }

                            String summary = details.length() > 60
                                    ? details.substring(0, 57) + "..."
                                    : details;

                            rows.add(new AnnouncementRow(cId, cName, type, details, summary));
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading clubActivity", e);
        }

    }

}
