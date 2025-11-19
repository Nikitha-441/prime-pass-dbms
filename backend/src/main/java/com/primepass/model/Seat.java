package com.primepass.model;

public class Seat {
    private Integer seatId;
    private Integer venueId;
    private String seatNumber;
    private String status;

    public Seat() {}

    public Seat(Integer seatId, Integer venueId, String seatNumber, String status) {
        this.seatId = seatId;
        this.venueId = venueId;
        this.seatNumber = seatNumber;
        this.status = status;
    }

    public Integer getSeatId() { return seatId; }
    public void setSeatId(Integer seatId) { this.seatId = seatId; }

    public Integer getVenueId() { return venueId; }
    public void setVenueId(Integer venueId) { this.venueId = venueId; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

