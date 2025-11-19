package com.primepass.controller;

import com.primepass.model.Venue;
import com.primepass.service.VenueService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/venues")
public class VenueController {

    @Autowired
    private VenueService venueService;

    @GetMapping
    public ResponseEntity<List<Venue>> getVenues() {
        return ResponseEntity.ok(venueService.getVenues());
    }

    @PostMapping
    public ResponseEntity<Venue> createVenue(@RequestBody Map<String, Object> request,
                                             HttpServletRequest httpRequest) {
        if (!"admin".equals(httpRequest.getAttribute("role"))) {
            return ResponseEntity.status(403).build();
        }
        Venue venue = venueService.createVenue(
                (String) request.get("name"),
                (String) request.get("location"),
                ((Number) request.get("totalSeats")).intValue()
        );
        return ResponseEntity.ok(venue);
    }

    @PutMapping("/{venueId}")
    public ResponseEntity<Void> updateVenue(@PathVariable Integer venueId,
                                            @RequestBody Map<String, Object> request,
                                            HttpServletRequest httpRequest) {
        if (!"admin".equals(httpRequest.getAttribute("role"))) {
            return ResponseEntity.status(403).build();
        }
        venueService.updateVenue(
                venueId,
                (String) request.get("name"),
                (String) request.get("location"),
                ((Number) request.get("totalSeats")).intValue()
        );
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{venueId}")
    public ResponseEntity<Void> deleteVenue(@PathVariable Integer venueId,
                                            HttpServletRequest httpRequest) {
        if (!"admin".equals(httpRequest.getAttribute("role"))) {
            return ResponseEntity.status(403).build();
        }
        venueService.deleteVenue(venueId);
        return ResponseEntity.ok().build();
    }
}

