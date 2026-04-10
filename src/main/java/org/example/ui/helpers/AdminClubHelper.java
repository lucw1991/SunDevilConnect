package org.example.ui.helpers;


import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextArea;
import org.example.domain.User;
import org.example.repository.InMemoryClubRepo;
import org.example.ui.admin_tableconfig.ClubRow;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.example.ui.admin_tableconfig.ClubsConfig.loadClubs;


public class AdminClubHelper {

    private final InMemoryClubRepo cRepo;
    private final AdminUserHelper userHelper;
    private final TextArea details;
    private final TextArea notifications;
    private final ObservableList<ClubRow> cRows;
    private final User user;
    private final String adminUserId;

    public AdminClubHelper(InMemoryClubRepo cRepo,
                           AdminUserHelper userHelper,
                           TextArea details,
                           TextArea notifications,
                           ObservableList<ClubRow> cRows, User user, String adminUserId) {
        this.cRepo = cRepo;
        this.userHelper = userHelper;
        this.details = details;
        this.notifications = notifications;
        this.cRows = cRows;
        this.user = user;
        this.adminUserId = adminUserId;
    }

    public void deleteClub(ClubRow row) {
        if (row == null) {
            return;
        }

        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Delete Club");
        a.setHeaderText("Delete Club: " + row.name());
        a.setContentText("Are you sure you want to delete this club?");
        Optional<ButtonType> choice = a.showAndWait();
        if (choice.isEmpty() || choice.get() != ButtonType.OK) {
            return;
        }

        File file = new File("src/main/resources/clubs.json");
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
                if (!cId.equals(row.id())) {
                    updatedClubs.put(cObj);
                }
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

        notifications.appendText("Deleted Club: "
                + row.name()
                + " ("
                + row.id()
                + ")\n");

        loadClubs(cRepo, cRows, user, adminUserId);
        details.clear();
    }

    public void showClubDetails(ClubRow row) {
        if (row == null) {
            details.clear();
            return;
        }

        JSONArray cArray = cRepo.readClubs();
        JSONObject found = null;
        JSONArray members = null;

        for (int i = 0; i < cArray.length(); i++) {
            JSONObject obj = cArray.getJSONObject(i);
            String cId = obj.optString("id", "");
            if (cId.equals(row.id())) {
                found = obj;
                members = obj.optJSONArray("members");
                break;
            }
        }

        if (found == null) {
            details.setText("Club not found in clubs.json for id: "
                    + row.id());
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Club: ").append(row.name())
                .append(" (").append(row.id()).append(")\n\n")
                .append("Description:\n").append(row.description()).append("\n\n")
                .append("Member Count: ").append(row.memberCount()).append("\n\n");

        if (members != null && !members.isEmpty()) {
            sb.append("Members:\n");
            for (int i = 0; i < members.length(); i++) {
                String id = members.getString(i);
                String name = userHelper.nameForUserID(id);
                sb.append("  - ").append(name).append(" (").append(id).append(")\n");
            }
        }

        details.setText(sb.toString());
    }

    public void manageMembers(ClubRow row) {

        JSONArray cArray = cRepo.readClubs();
        JSONArray updatedClubs;
        JSONArray members = null;
        int cIndex = -1;

        for (int i = 0; i < cArray.length(); i++) {
            JSONObject cObj = cArray.getJSONObject(i);
            String cId = cObj.optString("id", "");
            if (cId.equals(row.id())) {
                members = cObj.optJSONArray("members");
                cIndex = i;
                break;
            }
        }

        if (members == null || members.isEmpty()) {
            details.setText("No members found for Club: \n"
                    + row.name()
                    + " (" + row.id() + ") \n");
            return;
        }

        List<String> memIds = new ArrayList<>();
        for (int i = 0; i < members.length(); i++) {
            memIds.add(members.getString(i));
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        dialog.getItems().addAll(memIds.stream()
                .map(id -> userHelper.nameForUserID(id) + " (" + id + ")")
                .toList());
        dialog.setTitle("Manage Members");
        dialog.setHeaderText("Select a member to remove from " + row.name());
        dialog.setContentText("Members:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        String selectedEntry = result.get();
        String toRemove = selectedEntry.substring(selectedEntry.lastIndexOf('(') + 1,
                selectedEntry.length() - 1);

        JSONArray newMems = new JSONArray();
        for (int i = 0; i < members.length(); i++) {
            String id = members.getString(i);
            if (!id.equals(toRemove)) {
                newMems.put(id);
            }
        }

        updatedClubs = new JSONArray();
        for (int i = 0; i < cArray.length(); i++) {
            JSONObject cObj = cArray.getJSONObject(i);
            if (i == cIndex) {
                JSONObject updatedClub = new JSONObject(cObj.toString());
                updatedClub.put("members", newMems);
                updatedClubs.put(updatedClub);
            } else {
                updatedClubs.put(cObj);
            }
        }

        cRepo.writeClubs(updatedClubs);

        notifications.appendText("Member has been deleted: "
                + userHelper.nameForUserID(toRemove)
                + " from Club: "
                + row.name()
                + " (" + row.id() + " )\n");

        loadClubs(cRepo, cRows, user, adminUserId);

        StringBuilder sb = new StringBuilder();
        sb.append("Club: ").append(row.name())
                .append(" (")
                .append(row.id())
                .append(")\n\n");

        sb.append("Members:\n");
        for (int i = 0; i < newMems.length(); i++) {
            String id = newMems.getString(i);
            sb.append("  - ").append(userHelper.nameForUserID(id)).append("\n");
        }

        details.setText(sb.toString());
    }

}
