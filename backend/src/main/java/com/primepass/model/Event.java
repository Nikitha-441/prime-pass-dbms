package com.primepass.model;

public class Event {
    private Integer eventId;
    private String eventName;
    private String description;
    private Double basePrice;
    private Integer organizerId;
    private Integer categoryId;
    private String categoryName;
    private String organizerName;
    private java.time.LocalDateTime nextShowDate;
    private String nextVenue;

    public Event() {}

    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getBasePrice() { return basePrice; }
    public void setBasePrice(Double basePrice) { this.basePrice = basePrice; }

    public Integer getOrganizerId() { return organizerId; }
    public void setOrganizerId(Integer organizerId) { this.organizerId = organizerId; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }

    public java.time.LocalDateTime getNextShowDate() { return nextShowDate; }
    public void setNextShowDate(java.time.LocalDateTime nextShowDate) { this.nextShowDate = nextShowDate; }

    public String getNextVenue() { return nextVenue; }
    public void setNextVenue(String nextVenue) { this.nextVenue = nextVenue; }
}

