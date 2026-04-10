package org.example.ui.admin_tableconfig;

import javafx.beans.property.SimpleStringProperty;

public class AnnouncementRow {

    private final SimpleStringProperty clubId;
    private final SimpleStringProperty clubName;
    private final SimpleStringProperty type;
    private final SimpleStringProperty details;
    private final SimpleStringProperty summary;

    public AnnouncementRow(String clubId, String clubName,
                           String type, String details,
                           String summary) {
        this.clubId = new SimpleStringProperty(clubId);
        this.clubName = new SimpleStringProperty(clubName);
        this.type = new SimpleStringProperty(type);
        this.details = new SimpleStringProperty(details);
        this.summary = new SimpleStringProperty(summary);
    }

    public String clubId() {
        return clubId.get();
    }

    public String clubName() {
        return clubName.get();
    }

    public String type() {
        return type.get();
    }

    public String details() {
        return details.get();
    }

    public String summary() {
        return summary.get();
    }


    public SimpleStringProperty clubIdProperty() {
        return clubId;
    }

    public SimpleStringProperty clubNameProperty() {
        return clubName;
    }

    public SimpleStringProperty typeProperty() {
        return type;
    }

    public SimpleStringProperty detailsProperty() {
        return details;
    }

    public SimpleStringProperty summaryProperty() {
        return summary;
    }

}
