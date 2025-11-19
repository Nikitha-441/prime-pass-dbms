package com.primepass.controller;

import com.primepass.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBooking(@RequestBody Map<String, Object> request,
                                                            HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute("userId");
        Integer showtimeId = ((Number) request.get("showtimeId")).intValue();
        @SuppressWarnings("unchecked")
        List<Integer> seatIds = (List<Integer>) request.get("seatIds");
        String paymentMethod = (String) request.get("paymentMethod");

        Map<String, Object> result = bookingService.createBooking(userId, showtimeId, seatIds, paymentMethod);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(400).body(result);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getBookingHistory(HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(bookingService.getBookingHistory(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllBookings(HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        // Admin can see all bookings
        return ResponseEntity.ok(bookingService.getBookingHistory(null));
    }
}

