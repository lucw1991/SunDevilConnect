package org.example.service;

import org.example.domain.Club;
import org.example.domain.Registration;
import org.example.domain.SignUp;
import org.example.observer.Observer;
import org.example.observer.Subject;
import org.example.repository.ClubRepo;
import org.example.repository.InMemoryClubRepo;
import org.example.repository.SignUpRepo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public class SignUpService implements Subject {

    private final SignUpRepo signUps;
    private final ClubRepo clubs;
    private final Set<Observer> observers = new HashSet<>();

    public SignUpService(SignUpRepo signUps, ClubRepo clubs) {
        this.signUps = signUps;
        this.clubs = clubs;
    }

    public SignUp signUp(String clubId, String userId) {
        Club c = clubs.findById(clubId)
                .orElseThrow(() -> new NoSuchElementException("Club not found."));

        System.out.println("SignUp processing...");
        InMemoryClubRepo inMemoryClubRepo = new InMemoryClubRepo();
        JSONArray clubs = inMemoryClubRepo.readClubs();
        JSONObject club = new JSONObject();
        JSONArray clubMembers = new JSONArray();

        for (int i = 0; i < clubs.length(); i++) {
            club = clubs.getJSONObject(i);

            if (club.get("id").equals(clubId)) {
                clubMembers = club.getJSONArray("members");
                break;
            }
        }

        boolean memSignedUp = false;
        for (int j = 0; j < clubMembers.length(); j++) {
            String member = clubMembers.getString(j);

            if (member.equals(userId)) {
                memSignedUp = true;
                break;
            }
        }

        if (!memSignedUp) {
            System.out.println("Before signup: " + club);
            club.getJSONArray("members").put(userId);
            System.out.println("After registration: " + club);
            inMemoryClubRepo.writeClubs(clubs);
            notifyObservers("Sign-up confirmed for club: " + c.getName());
        } else {
            notifyObservers("Already a member of club: " + c.getName());
        }

        SignUp s = signUps.save(new SignUp(clubId, userId));
        return s;
    }

    // Getter for User signups
    public List<SignUp> getUserSignUp(String userId) {
        return signUps.findByUserId(userId);
    }

    @Override
    public void attach(Observer o) {
        observers.add(o);
    }

    @Override
    public void detach(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(String message) {
        observers.forEach(o -> o.update(message));
    }
}
