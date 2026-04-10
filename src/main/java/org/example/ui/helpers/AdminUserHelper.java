package org.example.ui.helpers;

import org.example.domain.User;
import org.example.repository.InMemoryUserRepo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


// Helper for looking up user display names
public class AdminUserHelper {

    private final InMemoryUserRepo uRepo;
    private Map<String, String> nameById = null;

    public AdminUserHelper(InMemoryUserRepo uRepo) {
        this.uRepo = uRepo;
    }

    public String displayName(String userId) {
        // Shows id if no name for some reason.
        return uRepo.findById(userId).map(User::getName).orElse(userId);
    }

    public String nameForUserID(String userId) {
        return getNameById().getOrDefault(userId, userId);
    }

    private Map<String, String> getNameById() {

        if (nameById != null) {
            return nameById;
        }

        Map<String, String> result = new HashMap<>();
        File file = new File("src/main/resources/users.json");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String json = br.readLine();
            if (json == null || json.isEmpty()) {
                return result;
            }
            JSONArray uArray = new JSONArray(json);
            for (int i = 0; i < uArray.length(); i++) {
                JSONObject uObj = uArray.getJSONObject(i);
                String id = uObj.optString("id", "");
                String name = uObj.optString("username", "");
                if (!id.isEmpty() && !name.isEmpty()) {
                    result.put(id, name);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        nameById = result;
        return nameById;
    }

}
