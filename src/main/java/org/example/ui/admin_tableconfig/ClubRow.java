package org.example.ui.admin_tableconfig;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ClubRow {

    private final SimpleStringProperty id;
    private final SimpleStringProperty name;
    private final SimpleStringProperty description;
    private final SimpleIntegerProperty memberCount;

    public ClubRow(String id, String name, String description, int memberCount) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
        this.memberCount = new SimpleIntegerProperty(memberCount);
    }

    // Getters
    public String id() {
        return id.get();
    }

    public String name() {
        return name.get();
    }

    public String description() {
        return description.get();
    }

    public int memberCount() {
        return memberCount.get();
    }

    // Properties
    public SimpleStringProperty idProperty() {
        return id;
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public SimpleIntegerProperty memberCountProperty() {
        return memberCount;
    }

}
