package org.example.service;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClubActivityService {

    private static final String FILE_PATH = "src/main/resources/clubActivity.json";

    public List<String> getEventsForClub(String clubId) {
        return getActivitiesByType(clubId, "event");
    }

    public List<String> getAnnouncementsForClub(String clubId) {
        return getActivitiesByType(clubId, "announcement");
    }

    private List<String> getActivitiesByType(String clubId, String type) {
        List<String> results = new ArrayList<>();
        File file = new File(FILE_PATH);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String json = br.readLine();
            if (json == null || json.isEmpty()) return results;

            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject club = array.getJSONObject(i);
                if (!clubId.equals(club.optString("id"))) continue;

                JSONArray activityArray = club.optJSONArray("activity");
                if (activityArray == null) continue;

                for (int j = 0; j < activityArray.length(); j++) {
                    JSONObject act = activityArray.getJSONObject(j);
                    if (type.equalsIgnoreCase(act.optString("type"))) {
                        results.add(act.optString("details", ""));
                    }
                }
                break;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading clubActivity.json", e);
        }
        return results;
    }

    public void addAnnouncement(String clubId, String details) {
        addActivity(clubId, "announcement", details);
    }

    public void addEventUpdate(String clubId, String details) {
        addActivity(clubId, "event", details);
    }

    private void addActivity(String clubId, String type, String details) {
        File file = new File(FILE_PATH);
        JSONArray clubsArray;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String json = br.readLine();
            clubsArray = (json == null || json.isEmpty()) ? new JSONArray() : new JSONArray(json);
        } catch (IOException e) {
            clubsArray = new JSONArray();
        }

        boolean clubFound = false;
        JSONObject clubObj = null;

        for (int i = 0; i < clubsArray.length(); i++) {
            JSONObject c = clubsArray.getJSONObject(i);
            if (clubId.equals(c.optString("id"))) {
                clubFound = true;
                clubObj = c;
                break;
            }
        }

        if (!clubFound) {
            clubObj = new JSONObject();
            clubObj.put("id", clubId);
            clubObj.put("activity", new JSONArray());
            clubsArray.put(clubObj);
        }

        JSONArray activityArray = clubObj.optJSONArray("activity");
        if (activityArray == null) {
            activityArray = new JSONArray();
            clubObj.put("activity", activityArray);
        }

        JSONObject newActivity = new JSONObject();
        newActivity.put("type", type);
        newActivity.put("details", details);
        activityArray.put(newActivity);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(clubsArray.toString());
        } catch (IOException e) {
            throw new RuntimeException("Error writing clubActivity", e);
        }
    }

    public void deleteAnnouncement(String clubId, String details) {
        deleteActivity(clubId, "announcement", details);
    }

    public void deleteEventUpdate(String clubId, String details) {
        deleteActivity(clubId, "event", details);
    }

    private void deleteActivity(String clubId, String type, String details) {
        File file = new File(FILE_PATH);
        JSONArray clubsArray;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String json = br.readLine();
            if (json == null || json.isEmpty()) {
                return;
            }
            clubsArray = new JSONArray(json);
        } catch (IOException e) {
            return;
        }

        boolean changed = false;

        for (int i = 0; i < clubsArray.length(); i++) {
            JSONObject c = clubsArray.getJSONObject(i);
            if (!clubId.equals(c.optString("id"))) continue;

            JSONArray activityArray = c.optJSONArray("activity");
            if (activityArray == null) break;

            for (int j = 0; j < activityArray.length(); j++) {
                JSONObject act = activityArray.getJSONObject(j);
                if (type.equalsIgnoreCase(act.optString("type"))
                        && details.equals(act.optString("details"))) {
                    activityArray.remove(j);
                    changed = true;
                    break;
                }
            }
            break;
        }

        if (changed) {
            writeArrayToFile(clubsArray, file);
        }
    }

    private void writeArrayToFile(JSONArray array, File file) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(array.toString());
        } catch (IOException e) {
            throw new RuntimeException("Error writing clubActivity.json", e);
        }
    }

}
