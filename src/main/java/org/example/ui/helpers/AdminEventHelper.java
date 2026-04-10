package org.example.ui.helpers;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextArea;
import org.example.domain.User;
import org.example.repository.InMemoryEventRepo;
import org.example.ui.admin_tableconfig.EventRow;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.example.ui.admin_tableconfig.EventsConfig.loadEvents;


public class AdminEventHelper {

    private final InMemoryEventRepo eRepo;
    private final AdminUserHelper userHelper;
    private final TextArea details;
    private final TextArea notifications;
    private final ObservableList<EventRow> eRows;
    private final User user;
    private final String adminUserId;

    public AdminEventHelper(InMemoryEventRepo eRepo,
                            AdminUserHelper userHelper,
                            TextArea details,
                            TextArea notifications,
                            ObservableList<EventRow> eRows, User user, String adminUserId) {
        this.eRepo = eRepo;
        this.userHelper = userHelper;
        this.details = details;
        this.notifications = notifications;
        this.eRows = eRows;
        this.user = user;
        this.adminUserId = adminUserId;
    }

    public void deleteEvent(EventRow row) {
        if (row == null) {
            return;
        }

        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Delete Event");
        a.setHeaderText("Delete Event: " + row.title());
        a.setContentText("Are you sure you want to delete this event?");
        Optional<ButtonType> choice = a.showAndWait();
        if (choice.isEmpty() || choice.get() != ButtonType.OK) {
            return;
        }

        File file = new File("src/main/resources/events.json");
        JSONArray updatedEvents;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String json = br.readLine();
            if (json == null || json.isEmpty()) {
                return;
            }

            JSONArray eArray = new JSONArray(json);
            updatedEvents = new JSONArray();

            for (int i = 0; i < eArray.length(); i++) {
                JSONObject eObj = eArray.getJSONObject(i);
                String eId = eObj.optString("id", "");
                if (!eId.equals(row.id())) {
                    updatedEvents.put(eObj);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(updatedEvents.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        notifications.appendText("Deleted Event: "
                + row.title()
                + " ("
                + row.id()
                + ")\n");

        loadEvents(eRepo, eRows, user, adminUserId);
        details.clear();
    }

    public void showEventDetails(EventRow row) {
        if (row == null) {
            details.clear();
            return;
        }

        JSONArray eArray = eRepo.readEventsJSONFile();
        JSONObject found = null;
        JSONArray registrations = null;

        for (int i = 0; i < eArray.length(); i++) {
            JSONObject obj = eArray.getJSONObject(i);
            String eId = obj.optString("id", "");
            if (eId.equals(row.id())) {
                found = obj;
                registrations = obj.optJSONArray("registrants");
                break;
            }
        }

        if (found == null) {
            details.setText("Event not found in events.json for id: "
                    + row.id());
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Event: ").append(row.title())
                .append(" (").append(row.id()).append(")\n\n")
                .append("Date: ").append(row.date()).append("\n")
                .append("Time: ").append(row.time()).append("\n")
                .append("Status: ").append(row.status()).append("\n\n");

        if (registrations != null && !registrations.isEmpty()) {
            sb.append("Registrations:\n");
            for (int i = 0; i < registrations.length(); i++) {
                String id = registrations.getString(i);
                String name = userHelper.nameForUserID(id) + " (" + id + ")";
                sb.append("  - ").append(name).append("\n");
            }
        }

        details.setText(sb.toString());
    }

    public void manageRegistrants(EventRow row) {
        JSONArray eArray = eRepo.readEventsJSONFile();
        JSONArray registrations = null;
        int eIndex = -1;

        for (int i = 0; i < eArray.length(); i++) {
            JSONObject eObj = eArray.getJSONObject(i);
            String eId = eObj.optString("id", "");
            if (eId.equals(row.id())) {
                registrations = eObj.optJSONArray("registrants");
                eIndex = i;
                break;
            }
        }

        if (registrations == null || registrations.isEmpty()) {
            details.setText("No registrants found for Event: \n"
                    + row.title()
                    + " (" + row.id() + ") \n");
            return;
        }

        List<String> regIds = new ArrayList<>();
        for (int i = 0; i < registrations.length(); i++) {
            regIds.add(registrations.getString(i));
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        dialog.getItems().addAll(regIds.stream()
                .map(id -> userHelper.nameForUserID(id) + " (" + id + ")")
                .toList());
        dialog.setTitle("Manage Registrations");
        dialog.setHeaderText("Select a registrant to remove from " + row.title());
        dialog.setContentText("Registrants:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        String selectedEntry = result.get();
        String toRemove = selectedEntry.substring(
                selectedEntry.lastIndexOf('(') + 1,
                selectedEntry.length() - 1
        );

        JSONArray newRegs = new JSONArray();
        for (int i = 0; i < registrations.length(); i++) {
            String id = registrations.getString(i);
            if (!id.equals(toRemove)) {
                newRegs.put(id);
            }
        }

        JSONArray updatedEvents = new JSONArray();
        for (int i = 0; i < eArray.length(); i++) {
            JSONObject eObj = eArray.getJSONObject(i);
            if (i == eIndex) {
                JSONObject updatedEvent = new JSONObject(eObj.toString());
                updatedEvent.put("registrants", newRegs);
                updatedEvents.put(updatedEvent);
            } else {
                updatedEvents.put(eObj);
            }
        }

        File file = new File("src/main/resources/events.json");
        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(updatedEvents.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        notifications.appendText("Registrant has been deleted: "
                + userHelper.displayName(toRemove)
                + " from Event: "
                + row.title()
                + " (" + row.id() + " )\n");

        loadEvents(eRepo, eRows, user, adminUserId);

        StringBuilder sb = new StringBuilder();
        sb.append("Event: ").append(row.title())
                .append(" (").append(row.id()).append(")\n\n");
        sb.append("Members/Registrants:\n");
        for (int i = 0; i < newRegs.length(); i++) {
            String id = newRegs.getString(i);
            sb.append("  - ").append(userHelper.nameForUserID(id))
                    .append(" (").append(id).append(")\n");
        }
        details.setText(sb.toString());
    }

}
