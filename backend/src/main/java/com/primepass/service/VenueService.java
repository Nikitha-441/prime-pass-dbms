package com.primepass.service;

import com.primepass.dao.VenueDao;
import com.primepass.model.Venue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VenueService {

    @Autowired
    private VenueDao venueDao;

    public List<Venue> getVenues() {
        return venueDao.getAllVenues();
    }

    public Venue createVenue(String name, String location, Integer totalSeats) {
        return venueDao.createVenue(name, location, totalSeats);
    }

    public void updateVenue(Integer venueId, String name, String location, Integer totalSeats) {
        venueDao.updateVenue(venueId, name, location, totalSeats);
    }

    public void deleteVenue(Integer venueId) {
        venueDao.deleteVenue(venueId);
    }
}

