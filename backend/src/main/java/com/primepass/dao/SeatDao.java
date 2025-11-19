package com.primepass.dao;

import com.primepass.model.Seat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SeatDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Seat> seatRowMapper = new RowMapper<Seat>() {
        @Override
        public Seat mapRow(ResultSet rs, int rowNum) throws SQLException {
            Seat seat = new Seat();
            seat.setSeatId(rs.getInt("seat_id"));
            seat.setVenueId(rs.getInt("venue_id"));
            seat.setSeatNumber(rs.getString("seat_number"));
            seat.setStatus(rs.getString("status"));
            return seat;
        }
    };

    public List<Seat> getSeatsByShowtimeId(Integer showtimeId) {
        String sql = "SELECT s.seat_id, s.venue_id, s.seat_number, " +
                     "COALESCE(ss.status, 'available') AS status " +
                     "FROM Seat s " +
                     "JOIN Showtime st ON s.venue_id = st.venue_id " +
                     "LEFT JOIN Seat_Status ss ON s.seat_id = ss.seat_id AND st.showtime_id = ss.showtime_id " +
                     "WHERE st.showtime_id = ? " +
                     "ORDER BY s.seat_number";
        return jdbcTemplate.query(sql, seatRowMapper, showtimeId);
    }

    public void updateSeatStatus(Integer showtimeId, Integer seatId, String status) {
        String sql = "INSERT INTO Seat_Status (showtime_id, seat_id, status) " +
                     "VALUES (?, ?, ?) " +
                     "ON CONFLICT (showtime_id, seat_id) " +
                     "DO UPDATE SET status = ?";
        jdbcTemplate.update(sql, showtimeId, seatId, status, status);
    }

    public void blockSeat(Integer showtimeId, Integer seatId) {
        updateSeatStatus(showtimeId, seatId, "blocked");
    }

    public void unblockSeat(Integer showtimeId, Integer seatId) {
        updateSeatStatus(showtimeId, seatId, "available");
    }
}

