package org.example.ui.admin_tableconfig;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.example.domain.Role;
import org.example.domain.User;
import org.example.repository.InMemoryClubRepo;
import org.json.JSONArray;
import org.json.JSONObject;

public class ClubsConfig {

    private ClubsConfig() {
    }

    public static void configure(TableView<ClubRow> table, ObservableList<ClubRow> rows) {

        table.setItems(rows);

        TableColumn<ClubRow, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colId.setPrefWidth(120);

        TableColumn<ClubRow, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(c -> c.getValue().nameProperty());
        colName.setPrefWidth(220);

        TableColumn<ClubRow, Number> colMembers = new TableColumn<>("Members");
        colMembers.setCellValueFactory(c -> c.getValue().memberCountProperty());
        colMembers.setPrefWidth(90);

        table.getColumns().setAll(colId, colName, colMembers);
    }

    // Load from JSON
    public static void loadClubs(InMemoryClubRepo repo, ObservableList<ClubRow> rows, User user, String adminUserId) {

        rows.clear();

        JSONArray cArray = repo.readClubs();
        if (user.getRole() == Role.ADMIN) {
            for (int i = 0; i < cArray.length(); i++) {
                JSONObject obj = cArray.getJSONObject(i);
                String id = obj.optString("id", "");
                String name = obj.optString("name", "Unnamed Club");
                String description = obj.optString("description", "No Description");
                JSONArray members = obj.optJSONArray("members");
                int memberCount = (members == null) ? 0 : members.length();
                rows.add(new ClubRow(id, name, description, memberCount));
            }
        } else {
            for (int i = 0; i < cArray.length(); i++) {
                JSONObject obj = cArray.getJSONObject(i);
                JSONArray memberArray = obj.getJSONArray("clubLeader");

                for (int j = 0; j < memberArray.length(); j++) {
                    String cLeaderId = memberArray.getString(j);

                    if (cLeaderId.equals(adminUserId) || user.getRole() == Role.ADMIN) {
                        String id = obj.optString("id", "");
                        String name = obj.optString("name", "Unnamed Club");
                        String description = obj.optString("description", "No Description");
                        JSONArray members = obj.optJSONArray("members");
                        int memberCount = (members == null) ? 0 : members.length();
                        rows.add(new ClubRow(id, name, description, memberCount));
                        break;
                    }
                }
            }
        }
    }
}