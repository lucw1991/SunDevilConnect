package org.example.service;

import org.example.domain.Club;
import org.example.domain.Event;
import org.example.repository.InMemoryClubRepo;
import org.example.repository.InMemoryEventRepo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SeedJson {

    // Utility to prevent instantiation
    private SeedJson() {}

    // Get Events from the JSON file
    public static void seedEvents(InMemoryEventRepo eventRepo) {
        JSONArray eArray = eventRepo.readEventsJSONFile();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("MM-dd-yyyy");

        for (int i = 0; i < eArray.length(); i++) {
            JSONObject obj = eArray.getJSONObject(i);

            String id = obj.getString("id");
            String clubId = obj.getString("clubId");

            String title = obj.optString("title", obj.optString("tile", "Untitled Event"));
            String dateStr = obj.getString("date");
            LocalDate date = LocalDate.parse(dateStr, df);

            // These aren't in JSON yet but I feel like we will want to use them as we continue along
            String location = "TBD";

            // Added into JSON
            double cost = obj.getDouble("cost");
            int popularity = obj.getInt("popularity");
            int capacity = obj.getInt("capacity");
            String category = obj.optString("category", "General");

            // Quick way to add category into it for now. Will add into the constructor once tested.
            Event event = new Event(id, clubId, title, date, location, cost, popularity, capacity, category);

            eventRepo.save(event);
        }
    }

    // Get Clubs from JSON
    public static void seedClubs(InMemoryClubRepo clubRepo) {
        JSONArray clubsArray = clubRepo.readClubs();

        for (int i  = 0; i < clubsArray.length(); i++) {
            JSONObject obj = clubsArray.getJSONObject(i);

            String id = obj.getString("id");
            String name = obj.optString("name", "Unnamed Club");
            String description = obj.optString("description", "");

            clubRepo.save(new Club(id, name, description));
        }
    }

}
