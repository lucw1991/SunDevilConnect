package org.example.repository;

import org.example.domain.Event;
import org.json.JSONArray;

import java.io.*;
import java.util.*;

public class InMemoryEventRepo implements EventRepo {

    private final Map<String, Event> store = new HashMap<>();

    @Override
    public Event save(Event e) {
        store.put(e.getId(), e);
        return e;
    }

    @Override
    public Optional<Event> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Event> findAll() {
        return new ArrayList<>(store.values());
    }

    public JSONArray readEventsJSONFile() {
        // Declare JSONArray to store the events information for the events.json file
        JSONArray events = new JSONArray();

        // Use try-catch block to read all the existing events from the events.json file
        try {
            // Read from the events.json file
            FileReader fileReader = new FileReader("src/main/resources/events.json");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String eventData = bufferedReader.readLine();

            // Store the data from the file in JSONArray
            events = new JSONArray(eventData);
            // Close resource
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Return events information
        return events;
    }

    public void writeEventsJSONFile(JSONArray events) {
        // Use try-catch block to write the updated existing events to the events.json file
        try {
            FileWriter fileWriter = new FileWriter("src/main/resources/events.json");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            // Write the updated events list to the file
            bufferedWriter.write(events.toString());

            // Close resource
            bufferedWriter.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
