package com.primepass.controller;

import com.primepass.model.Seat;
import com.primepass.service.SeatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/seats")
public class SeatController {
    @Autowired
    private SeatService seatService;

    @GetMapping
    public ResponseEntity<List<Seat>> getSeats(@RequestParam Integer showtimeId) {
        return ResponseEntity.ok(seatService.getSeatsByShowtimeId(showtimeId));
    }

    @PostMapping("/block")
    public ResponseEntity<Map<String, Object>> blockSeat(@RequestBody Map<String, Object> request,
                                                          HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        Integer showtimeId = ((Number) request.get("showtimeId")).intValue();
        Integer seatId = ((Number) request.get("seatId")).intValue();

        seatService.blockSeat(showtimeId, seatId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/unblock")
    public ResponseEntity<Map<String, Object>> unblockSeat(@RequestBody Map<String, Object> request,
                                                            HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        Integer showtimeId = ((Number) request.get("showtimeId")).intValue();
        Integer seatId = ((Number) request.get("seatId")).intValue();

        seatService.unblockSeat(showtimeId, seatId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

