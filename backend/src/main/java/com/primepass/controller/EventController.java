package com.primepass.controller;

import com.primepass.model.Event;
import com.primepass.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.primepass.model.Showtime;

@RestController
@RequestMapping("/events")
public class EventController {
    @Autowired
    private EventService eventService;

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<Map<String, Object>> getEventDetails(@PathVariable Integer eventId) {
        Map<String, Object> result = eventService.getEventDetails(eventId);
        if (result.containsKey("error")) {
            return ResponseEntity.status(404).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/showtimes/{showtimeId}")
    public ResponseEntity<Showtime> getShowtime(@PathVariable Integer showtimeId) {
        Showtime showtime = eventService.getShowtimeById(showtimeId);
        if (showtime == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(showtime);
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Map<String, Object> request,
                                            HttpServletRequest httpRequest) {
        // Check admin role
        String role = (String) httpRequest.getAttribute("role");
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        String eventName = (String) request.get("eventName");
        String description = (String) request.get("description");
        Double basePrice = ((Number) request.get("basePrice")).doubleValue();
        Integer organizerId = ((Number) request.get("organizerId")).intValue();
        Integer categoryId = ((Number) request.get("categoryId")).intValue();

        Event event = eventService.createEvent(eventName, description, basePrice, organizerId, categoryId);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<Void> updateEvent(@PathVariable Integer eventId,
                                           @RequestBody Map<String, Object> request,
                                           HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        String eventName = (String) request.get("eventName");
        String description = (String) request.get("description");
        Double basePrice = ((Number) request.get("basePrice")).doubleValue();

        eventService.updateEvent(eventId, eventName, description, basePrice);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Integer eventId,
                                            HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        eventService.deleteEvent(eventId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{eventId}/showtimes")
    public ResponseEntity<Map<String, Object>> createShowtime(@PathVariable Integer eventId,
                                                               @RequestBody Map<String, Object> request,
                                                               HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        Integer venueId = ((Number) request.get("venueId")).intValue();
        LocalDateTime showDate = LocalDateTime.parse((String) request.get("showDate"));
        Double price = ((Number) request.get("price")).doubleValue();

        var showtime = eventService.createShowtime(eventId, venueId, showDate, price);
        return ResponseEntity.ok(Map.of("showtime", showtime));
    }

    @PutMapping("/showtimes/{showtimeId}")
    public ResponseEntity<Void> updateShowtime(@PathVariable Integer showtimeId,
                                               @RequestBody Map<String, Object> request,
                                               HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        Integer venueId = ((Number) request.get("venueId")).intValue();
        LocalDateTime showDate = LocalDateTime.parse((String) request.get("showDate"));
        Double price = ((Number) request.get("price")).doubleValue();

        eventService.updateShowtime(showtimeId, venueId, showDate, price);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/showtimes/{showtimeId}")
    public ResponseEntity<Void> deleteShowtime(@PathVariable Integer showtimeId,
                                               HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        eventService.deleteShowtime(showtimeId);
        return ResponseEntity.ok().build();
    }
}


