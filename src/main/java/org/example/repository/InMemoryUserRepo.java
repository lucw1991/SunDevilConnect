package org.example.repository;

import org.example.domain.User;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;


public class InMemoryUserRepo implements UserRepo {

    private final Map<String, User> store = new HashMap<>();

    @Override
    public User save(User u) {
        store.put(u.getId(), u);
        return u;
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }

    public JSONArray readUserJSONFile() {
        // Declare JSONArray to store user information from the users.json file
        JSONArray users = new JSONArray();

        // Use try-catch block to read the existing users from the users.json file
        try {
            // Read from the users.json file
            FileReader fileReader = new FileReader("src/main/resources/users.json");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String eventData = bufferedReader.readLine();

            // Store the data from the file in JSONArray
            users = new JSONArray(eventData);
            // Close resource
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Return events information
        return users;
    }

}
