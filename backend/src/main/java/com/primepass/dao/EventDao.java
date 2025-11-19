package com.primepass.dao;

import com.primepass.model.Event;
import com.primepass.model.Showtime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class EventDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Event> eventRowMapper = new RowMapper<Event>() {
        @Override
        public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
            Event event = new Event();
            event.setEventId(rs.getInt("event_id"));
            event.setEventName(rs.getString("event_name"));
            event.setDescription(rs.getString("description"));
            event.setBasePrice(rs.getDouble("base_price"));
            try { event.setOrganizerId(rs.getInt("organizer_id")); } catch (SQLException ignored) {}
            try { event.setCategoryId(rs.getInt("category_id")); } catch (SQLException ignored) {}
            event.setCategoryName(rs.getString("category_name"));
            event.setOrganizerName(rs.getString("organizer_name"));
            try {
                Timestamp nextTs = rs.getTimestamp("next_show_date");
                if (nextTs != null) {
                    event.setNextShowDate(nextTs.toLocalDateTime());
                }
            } catch (SQLException ignored) {}
            try { event.setNextVenue(rs.getString("next_venue")); } catch (SQLException ignored) {}
            try { event.setNextVenueLocation(rs.getString("next_venue_location")); } catch (SQLException ignored) {}
            return event;
        }
    };

    private final RowMapper<Showtime> showtimeRowMapper = new RowMapper<Showtime>() {
        @Override
        public Showtime mapRow(ResultSet rs, int rowNum) throws SQLException {
            Showtime showtime = new Showtime();
            showtime.setShowtimeId(rs.getInt("showtime_id"));
            showtime.setEventId(rs.getInt("event_id"));
            showtime.setVenueId(rs.getInt("venue_id"));
            Timestamp ts = rs.getTimestamp("show_date");
            showtime.setShowDate(ts != null ? ts.toLocalDateTime() : null);
            showtime.setPrice(rs.getDouble("price"));
            showtime.setVenueName(rs.getString("venue_name"));
            showtime.setVenueLocation(rs.getString("venue_location"));
            return showtime;
        }
    };

    public List<Event> getAllEvents() {
        String sql = "SELECT * FROM user_events_view ORDER BY event_id";
        return jdbcTemplate.query(sql, eventRowMapper);
    }

    public Event getEventById(Integer eventId) {
        String sql = "SELECT e.*, c.category_name, o.name AS organizer_name " +
                     "FROM Event e " +
                     "JOIN Category c ON e.category_id = c.category_id " +
                     "JOIN Organizer o ON e.organizer_id = o.organizer_id " +
                     "WHERE e.event_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, eventRowMapper, eventId);
        } catch (Exception e) {
            return null;
        }
    }

    public List<Showtime> getShowtimesByEventId(Integer eventId) {
        String sql = "SELECT st.*, v.name AS venue_name, v.location AS venue_location " +
                     "FROM Showtime st " +
                     "JOIN Venue v ON st.venue_id = v.venue_id " +
                     "WHERE st.event_id = ? " +
                     "ORDER BY st.show_date";
        return jdbcTemplate.query(sql, showtimeRowMapper, eventId);
    }

    public Event createEvent(String eventName, String description, Double basePrice, 
                            Integer organizerId, Integer categoryId) {
        String sql = "INSERT INTO Event (event_name, description, base_price, organizer_id, category_id) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING *";
        // Note: This returns only event fields, need to join for full details
        return getEventById(jdbcTemplate.queryForObject(
            "INSERT INTO Event (event_name, description, base_price, organizer_id, category_id) " +
            "VALUES (?, ?, ?, ?, ?) RETURNING event_id",
            Integer.class, eventName, description, basePrice, organizerId, categoryId
        ));
    }

    public void updateEvent(Integer eventId, String eventName, String description, Double basePrice) {
        String sql = "UPDATE Event SET event_name = ?, description = ?, base_price = ? WHERE event_id = ?";
        jdbcTemplate.update(sql, eventName, description, basePrice, eventId);
    }

    public void deleteEvent(Integer eventId) {
        String sql = "DELETE FROM Event WHERE event_id = ?";
        jdbcTemplate.update(sql, eventId);
    }

    public Showtime createShowtime(Integer eventId, Integer venueId, LocalDateTime showDate, Double price) {
        String sql = "INSERT INTO Showtime (event_id, venue_id, show_date, price) " +
                     "VALUES (?, ?, ?, ?) RETURNING showtime_id";
        Integer showtimeId = jdbcTemplate.queryForObject(
            sql, Integer.class, eventId, venueId, Timestamp.valueOf(showDate), price
        );

        // Initialize Seat_Status for all seats of the venue so booking can lock rows
        jdbcTemplate.update(
            "INSERT INTO Seat_Status (showtime_id, seat_id, status) " +
            "SELECT ?, s.seat_id, 'available' FROM Seat s WHERE s.venue_id = ? " +
            "ON CONFLICT (showtime_id, seat_id) DO NOTHING",
            showtimeId, venueId
        );

        return getShowtimeById(showtimeId);
    }

    public Showtime getShowtimeById(Integer showtimeId) {
        String sql = "SELECT st.*, v.name AS venue_name, v.location AS venue_location " +
                     "FROM Showtime st " +
                     "JOIN Venue v ON st.venue_id = v.venue_id " +
                     "WHERE st.showtime_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, showtimeRowMapper, showtimeId);
        } catch (Exception e) {
            return null;
        }
    }

    public void updateShowtime(Integer showtimeId, Integer venueId, LocalDateTime showDate, Double price) {
        String sql = "UPDATE Showtime SET venue_id = ?, show_date = ?, price = ? WHERE showtime_id = ?";
        jdbcTemplate.update(sql, venueId, Timestamp.valueOf(showDate), price, showtimeId);
    }

    public void deleteShowtime(Integer showtimeId) {
        String sql = "DELETE FROM Showtime WHERE showtime_id = ?";
        jdbcTemplate.update(sql, showtimeId);
    }
}

