package com.primepass.dao;

import com.primepass.model.Venue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VenueDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final BeanPropertyRowMapper<Venue> mapper = new BeanPropertyRowMapper<>(Venue.class);

    public List<Venue> getAllVenues() {
        return jdbcTemplate.query("SELECT * FROM Venue ORDER BY name", mapper);
    }

    public Venue createVenue(String name, String location, Integer totalSeats) {
        Integer id = jdbcTemplate.queryForObject(
                "INSERT INTO Venue (name, location, total_seats) VALUES (?, ?, ?) RETURNING venue_id",
                Integer.class, name, location, totalSeats
        );
        return getVenueById(id);
    }

    public Venue getVenueById(Integer venueId) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM Venue WHERE venue_id = ?", mapper, venueId);
        } catch (Exception ex) {
            return null;
        }
    }

    public void updateVenue(Integer venueId, String name, String location, Integer totalSeats) {
        jdbcTemplate.update("UPDATE Venue SET name = ?, location = ?, total_seats = ? WHERE venue_id = ?",
                name, location, totalSeats, venueId);
    }

    public void deleteVenue(Integer venueId) {
        jdbcTemplate.update("DELETE FROM Venue WHERE venue_id = ?", venueId);
    }
}

