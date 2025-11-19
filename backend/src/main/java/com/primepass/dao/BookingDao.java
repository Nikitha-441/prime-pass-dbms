package com.primepass.dao;

import com.primepass.model.Booking;
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
public class BookingDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Booking> bookingRowMapper = new RowMapper<Booking>() {
        @Override
        public Booking mapRow(ResultSet rs, int rowNum) throws SQLException {
            Booking booking = new Booking();
            booking.setBookingId(rs.getInt("booking_id"));
            booking.setUserId(rs.getInt("user_id"));
            Timestamp ts = rs.getTimestamp("booking_time");
            booking.setBookingTime(ts != null ? ts.toLocalDateTime() : null);
            booking.setTotalAmount(rs.getDouble("total_amount"));
            booking.setBookingCode(rs.getString("booking_code"));
            booking.setPaymentStatus(rs.getString("payment_status"));
            return booking;
        }
    };

    public List<Booking> getBookingsByUserId(Integer userId) {
        String sql = "SELECT * FROM Booking WHERE user_id = ? ORDER BY booking_time DESC";
        return jdbcTemplate.query(sql, bookingRowMapper, userId);
    }

    public List<Booking> getAllBookings() {
        String sql = "SELECT * FROM Booking ORDER BY booking_time DESC";
        return jdbcTemplate.query(sql, bookingRowMapper);
    }

    public Booking getBookingById(Integer bookingId) {
        String sql = "SELECT * FROM Booking WHERE booking_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, bookingRowMapper, bookingId);
        } catch (Exception e) {
            return null;
        }
    }
}

