package com.primepass.service;

import com.primepass.dao.BookingDao;
import com.primepass.dao.SeatDao;
import com.primepass.dao.EventDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class BookingService {
    @Autowired
    private BookingDao bookingDao;

    @Autowired
    private SeatDao seatDao;

    @Autowired
    private EventDao eventDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public Map<String, Object> createBooking(Integer userId, Integer showtimeId, 
                                             List<Integer> seatIds, String paymentMethod) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get showtime price
            Double price = jdbcTemplate.queryForObject(
                "SELECT price FROM Showtime WHERE showtime_id = ?", 
                Double.class, showtimeId
            );
            
            if (price == null) {
                response.put("success", false);
                response.put("message", "Showtime not found");
                return response;
            }

            Double totalAmount = price * seatIds.size();

            // Generate unique booking code
            String bookingCode = generateBookingCode();

            // Create booking
            Integer bookingId = jdbcTemplate.queryForObject(
                "INSERT INTO Booking (user_id, total_amount, booking_code, payment_status) " +
                "VALUES (?, ?, ?, 'pending') RETURNING booking_id",
                Integer.class, userId, totalAmount, bookingCode
            );

            // Book seats and update status (with row-level locking)
            for (Integer seatId : seatIds) {
                // Check and lock seat
                Integer locked = jdbcTemplate.queryForObject(
                    "SELECT seat_id FROM Seat_Status " +
                    "WHERE showtime_id = ? AND seat_id = ? AND status = 'available' " +
                    "FOR UPDATE",
                    Integer.class, showtimeId, seatId
                );

                if (locked == null) {
                    throw new RuntimeException("Seat " + seatId + " is not available");
                }

                // Insert booked seat
                jdbcTemplate.update(
                    "INSERT INTO Booked_Seats (booking_id, showtime_id, seat_id, price_paid) " +
                    "VALUES (?, ?, ?, ?)",
                    bookingId, showtimeId, seatId, price
                );

                // Update seat status
                jdbcTemplate.update(
                    "INSERT INTO Seat_Status (showtime_id, seat_id, status) " +
                    "VALUES (?, ?, 'booked') " +
                    "ON CONFLICT (showtime_id, seat_id) " +
                    "DO UPDATE SET status = 'booked'",
                    showtimeId, seatId
                );
            }

            // Create payment record
            jdbcTemplate.update(
                "INSERT INTO Payment (booking_id, amount, method) " +
                "VALUES (?, ?, ?)",
                bookingId, totalAmount, paymentMethod
            );

            // Update booking status
            jdbcTemplate.update(
                "UPDATE Booking SET payment_status = 'completed' WHERE booking_id = ?",
                bookingId
            );

            response.put("success", true);
            response.put("bookingId", bookingId);
            response.put("bookingCode", bookingCode);
            response.put("totalAmount", totalAmount);
            response.put("message", "Booking created successfully");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Booking failed: " + e.getMessage());
            throw e; // This will trigger rollback
        }

        return response;
    }

    private String generateBookingCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < 16; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        // Ensure uniqueness
        while (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM Booking WHERE booking_code = ?", 
            Integer.class, code.toString()) > 0) {
            code = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                code.append(chars.charAt(random.nextInt(chars.length())));
            }
        }
        
        return code.toString();
    }

    public List<Map<String, Object>> getBookingHistory(Integer userId) {
        if (userId == null) {
            // Admin: use admin_bookings_view to include user info and aggregates
            String sql = "SELECT * FROM admin_bookings_view ORDER BY booking_time DESC";
            return jdbcTemplate.query(sql, getBookingRowMapper(true));
        } else {
            // User: use user_booking_history_view filtered by user_id
            String sql = "SELECT * FROM user_booking_history_view WHERE user_id = ? ORDER BY booking_time DESC";
            return jdbcTemplate.query(sql, getBookingRowMapper(false), userId);
        }
    }

    private org.springframework.jdbc.core.RowMapper<Map<String, Object>> getBookingRowMapper(boolean includeUserInfo) {
        return (rs, rowNum) -> {
            Map<String, Object> booking = new HashMap<>();
            booking.put("bookingId", rs.getInt("booking_id"));
            booking.put("bookingCode", rs.getString("booking_code"));
            booking.put("bookingTime", rs.getTimestamp("booking_time"));
            booking.put("totalAmount", rs.getDouble("total_amount"));
            booking.put("paymentStatus", rs.getString("payment_status"));
            booking.put("eventName", rs.getString("event_name"));
            booking.put("categoryName", rs.getString("category_name"));
            booking.put("showDate", rs.getTimestamp("show_date"));
            booking.put("venueName", rs.getString("venue_name"));
            booking.put("seats", rs.getString("seats"));
            if (includeUserInfo) {
                booking.put("userName", rs.getString("user_name"));
                booking.put("userEmail", rs.getString("user_email"));
                booking.put("seatCount", rs.getInt("seat_count"));
            }
            return booking;
        };
    }
}

