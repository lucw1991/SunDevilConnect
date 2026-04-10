package org.example.ui.admin_tableconfig;

import javafx.beans.property.SimpleStringProperty;

public class EventRow {

    private final SimpleStringProperty id;
    private final SimpleStringProperty title;
    private final SimpleStringProperty date;
    private final SimpleStringProperty time;
    private final SimpleStringProperty status;

    public EventRow(String id, String title, String date, String time, String status) {
        this.id = new SimpleStringProperty(id);
        this.title = new SimpleStringProperty(title);
        this.date = new SimpleStringProperty(date);
        this.time = new SimpleStringProperty(time);
        this.status = new SimpleStringProperty(status);
    }

    public String id() {
        return id.get();
    }

    public String title() {
        return title.get();
    }

    public String date() {
        return date.get();
    }

    public String time() {
        return time.get();
    }

    public String status() {
        return status.get();
    }


    public SimpleStringProperty idProperty() {
        return id;
    }

    public SimpleStringProperty titleProperty() {
        return title;
    }

    public SimpleStringProperty dateProperty() {
        return date;
    }

    public SimpleStringProperty timeProperty() {
        return time;
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

}
