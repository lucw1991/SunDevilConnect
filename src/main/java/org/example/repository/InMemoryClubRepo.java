package org.example.repository;

import org.example.domain.Club;
import org.json.JSONArray;

import java.io.*;
import java.util.*;

public class InMemoryClubRepo implements ClubRepo {

    private final Map<String, Club> store = new HashMap<>();

    @Override
    public Club save(Club c) {
        store.put(c.getId(),c);
        return c;
    }

    @Override
    public Optional<Club> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Club> findAll() {
        return new ArrayList<>(store.values());
    }

    // JSON helpers
    public JSONArray readClubs(){
        JSONArray clubs = new JSONArray();
        try(BufferedReader br = new BufferedReader(
                                    new FileReader("src/main/resources/clubs.json")
                                )
        ) {
           String data = br.readLine();
           if (data != null && !data.isEmpty()) {
               clubs = new JSONArray(data);
           }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return clubs;
    }

    public void writeClubs(JSONArray clubs){
        try (BufferedWriter bw = new BufferedWriter(
                                    new FileWriter("src/main/resources/clubs.json")
                                )
        ) {
            bw.write(clubs.toString());
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
