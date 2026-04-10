package org.example.ui.helpers;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import org.example.domain.User;
import org.example.repository.InMemoryClubRepo;
import org.example.ui.admin_tableconfig.AnnouncementRow;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.Optional;

import static org.example.ui.admin_tableconfig.AnnouncementsConfig.loadAnnouncements;


public class AdminAnnouncementHelper {

    private final InMemoryClubRepo cRepo;
    private final TextArea details;
    private final TextArea notifications;
    private final ObservableList<AnnouncementRow> aRows;
    private final User user;
    private final String adminUserId;

    public AdminAnnouncementHelper(InMemoryClubRepo cRepo,
                                   TextArea details,
                                   TextArea notifications,
                                   ObservableList<AnnouncementRow> aRows, User user, String adminUserId) {
        this.cRepo = cRepo;
        this.details = details;
        this.notifications = notifications;
        this.aRows = aRows;
        this.user = user;
        this.adminUserId = adminUserId;
    }

    public void showAnnouncementDetails(AnnouncementRow row) {
        if (row == null) {
            details.clear();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Club: ").append(row.clubName())
                .append(" (").append(row.clubId()).append(")\n\n")
                .append("Announcement:\n")
                .append(row.details());

        details.setText(sb.toString());
    }

    public void deleteAnnouncement(AnnouncementRow row) {
        if (row == null) {
            return;
        }

        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Delete Announcement");
        a.setHeaderText("Delete Announcement for Club: " + row.clubName());
        a.setContentText("Are you sure you want to delete this announcement?");
        Optional<ButtonType> choice = a.showAndWait();
        if (choice.isEmpty() || choice.get() != ButtonType.OK) {
            return;
        }

        File file = new File("src/main/resources/clubActivity.json");
        JSONArray updatedClubs;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String json = br.readLine();
            if (json == null || json.isEmpty()) {
                return;
            }

            JSONArray cArray = new JSONArray(json);
            updatedClubs = new JSONArray();

            for (int i = 0; i < cArray.length(); i++) {
                JSONObject cObj = cArray.getJSONObject(i);
                String cId = cObj.optString("id", "");
                JSONArray activities = cObj.optJSONArray("activity");

                if (!cId.equals(row.clubId()) || activities == null) {
                    updatedClubs.put(cObj);
                    continue;
                }

                JSONArray newActivities = new JSONArray();
                for (int j = 0; j < activities.length(); j++) {
                    JSONObject activity = activities.getJSONObject(j);
                    String type = activity.optString("type", "");
                    String details = activity.optString("details", "");

                    if ("announcement".equalsIgnoreCase(type) && details.equals(row.details())) {
                        continue;
                    }
                    newActivities.put(activity);
                }

                JSONObject updatedClub = new JSONObject(cObj.toString());
                updatedClub.put("activity", newActivities);
                updatedClubs.put(updatedClub);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(updatedClubs.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        notifications.appendText("Deleted Announcement for Club: "
                + row.clubName()
                + " ("
                + row.clubId()
                + ") \n");

        loadAnnouncements(cRepo, aRows, user, adminUserId);
        details.clear();
    }

}
