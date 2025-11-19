package com.primepass.model;

import java.time.LocalDateTime;

public class Showtime {
    private Integer showtimeId;
    private Integer eventId;
    private Integer venueId;
    private LocalDateTime showDate;
    private Double price;
    private String venueName;
    private String venueLocation;
    private String eventName;

    public Showtime() {}

    public Integer getShowtimeId() { return showtimeId; }
    public void setShowtimeId(Integer showtimeId) { this.showtimeId = showtimeId; }

    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }

    public Integer getVenueId() { return venueId; }
    public void setVenueId(Integer venueId) { this.venueId = venueId; }

    public LocalDateTime getShowDate() { return showDate; }
    public void setShowDate(LocalDateTime showDate) { this.showDate = showDate; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }

    public String getVenueLocation() { return venueLocation; }
    public void setVenueLocation(String venueLocation) { this.venueLocation = venueLocation; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
}

