package org.example.api;

import org.example.service.ClubActivityService;
import java.util.List;



public class ClubActivityAPI {

    private final ClubActivityService service;

    public ClubActivityAPI(ClubActivityService service) {
        this.service = service;
    }

    public List<String> listEventsForClub(String clubId) {
        return service.getEventsForClub(clubId);
    }

    public List<String> listAnnouncementsForClub(String clubId) {
        return service.getAnnouncementsForClub(clubId);
    }

    public void postAnnouncementForClub(String clubId, String details) {
        service.addAnnouncement(clubId, details);
    }

    public void postEventUpdateForClub(String clubId, String details) {
        service.addEventUpdate(clubId, details);
    }

    public void deleteAnnouncementForClub(String clubId, String details) {
        service.deleteAnnouncement(clubId, details);
    }

    public void deleteEventEntryForClub(String clubId, String details) {
        service.deleteEventUpdate(clubId, details);
    }

}
