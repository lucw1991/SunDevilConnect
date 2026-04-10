package org.example.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class Event {

    private final String id;
    private final String clubId;
    private String title;
    private LocalDate dateTime;
    private String location;
    private double cost;
    private int popularity;
    private int capacity;
    private String category;

    public Event(String id, String clubId, String title, LocalDate dateTime, String location,
                 double cost, int popularity, int capacity, String category) {

        this.id = id;
        this.clubId = clubId;
        this.title = title;
        this.dateTime = dateTime;
        this.location = location;
        this.cost = cost;
        this.popularity = popularity;
        this.capacity = capacity;
        this.category = category;

    }

    public String getId() {
        return id;
    }
    public String getClubId() {
        return clubId;
    }
    public String getTitle() {
        return title;
    }
    public LocalDate getDateTime() {
        return dateTime;
    }
    public String getLocation() {
        return location;
    }
    public double getCost() {
        return cost;
    }
    public int getPopularity() {
        return popularity;
    }
    public int getCapacity() {
        return capacity;
    }
    public String getCategory() { return category; }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    public void setCategory(String category) { this.category = category; }
    public void setCost(double cost) { this.cost = cost; }

}
