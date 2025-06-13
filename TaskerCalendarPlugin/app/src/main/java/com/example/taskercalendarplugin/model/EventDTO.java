package com.example.taskercalendarplugin.model;

public class EventDTO {
    private long id;
    private String title;
    private String description;
    private long dtstart; // Start time in milliseconds since epoch
    private long dtend;   // End time in milliseconds since epoch
    private String eventLocation;
    private boolean allDay; // Added based on typical calendar event properties

    public EventDTO(long id, String title, String description, long dtstart, long dtend, String eventLocation, boolean allDay) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dtstart = dtstart;
        this.dtend = dtend;
        this.eventLocation = eventLocation;
        this.allDay = allDay;
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getDtstart() {
        return dtstart;
    }

    public long getDtend() {
        return dtend;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public boolean isAllDay() {
        return allDay;
    }

    // Setters (optional, depending on if you need to modify DTOs after creation)
    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDtstart(long dtstart) {
        this.dtstart = dtstart;
    }

    public void setDtend(long dtend) {
        this.dtend = dtend;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    @Override
    public String toString() {
        return "EventDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", dtstart=" + dtstart +
                ", dtend=" + dtend +
                ", eventLocation='" + eventLocation + '\'' +
                ", allDay=" + allDay +
                '}';
    }
}
