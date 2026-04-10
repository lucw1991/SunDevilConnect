package org.example.ui.admin_tableconfig;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.example.domain.Role;
import org.example.domain.User;
import org.example.repository.InMemoryClubRepo;
import org.example.repository.InMemoryEventRepo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EventsConfig {

    private EventsConfig() {}

    public static void configure(TableView<EventRow> table, ObservableList<EventRow> rows) {

        table.setItems(rows);

        TableColumn<EventRow, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colId.setPrefWidth(110);

        TableColumn<EventRow, String> colTitle = new TableColumn<>("Title");
        colTitle.setCellValueFactory(c -> c.getValue().titleProperty());
        colTitle.setPrefWidth(200);

        TableColumn<EventRow, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(c -> c.getValue().dateProperty());
        colDate.setPrefWidth(90);

        TableColumn<EventRow, String> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(c -> c.getValue().timeProperty());
        colTime.setPrefWidth(90);

        TableColumn<EventRow, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> c.getValue().statusProperty());
        colStatus.setPrefWidth(90);

        table.getColumns().setAll(colId, colTitle, colDate, colTime, colStatus);

    }

    // Load from JSON
    public static void loadEvents(InMemoryEventRepo repo, ObservableList<EventRow> rows, User user, String adminUserId) {

        rows.clear();

        // Determine clubs user is a leader of
        InMemoryClubRepo clubRepo = new InMemoryClubRepo();
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
            JSONArray eArray = repo.readEventsJSONFile();
            for (int i = 0; i < eArray.length(); i++) {
                JSONObject obj = eArray.getJSONObject(i);

                if (clubId.equals(obj.getString("clubId"))) {
                    String id = obj.optString("id", "");
                    String title = obj.optString("title",  // Since title is tile in JSON at the moment. I may be making this issue more annoying that it should have been from the beginning so I will go through and change these when the time comes.
                            obj.optString("tile",
                                    "Unnamed Event"));
                    String date = obj.optString("date", "");
                    String time = obj.optString("time", "");
                    String status = obj.optString("status", "");

                    rows.add(new EventRow(id, title, date, time, status));
                }
            }
        }
    }

}
