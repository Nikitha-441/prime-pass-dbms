package com.primepass.service;

import com.primepass.dao.EventDao;
import com.primepass.model.Event;
import com.primepass.model.Showtime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventService {
    @Autowired
    private EventDao eventDao;

    public List<Event> getAllEvents() {
        return eventDao.getAllEvents();
    }

    public Map<String, Object> getEventDetails(Integer eventId) {
        Map<String, Object> response = new HashMap<>();
        Event event = eventDao.getEventById(eventId);
        if (event == null) {
            response.put("error", "Event not found");
            return response;
        }
        
        List<Showtime> showtimes = eventDao.getShowtimesByEventId(eventId);
        response.put("event", event);
        response.put("showtimes", showtimes);
        return response;
    }

    public Event createEvent(String eventName, String description, Double basePrice, 
                            Integer organizerId, Integer categoryId) {
        return eventDao.createEvent(eventName, description, basePrice, organizerId, categoryId);
    }

    public void updateEvent(Integer eventId, String eventName, String description, Double basePrice) {
        eventDao.updateEvent(eventId, eventName, description, basePrice);
    }

    public void deleteEvent(Integer eventId) {
        eventDao.deleteEvent(eventId);
    }

    public Showtime createShowtime(Integer eventId, Integer venueId, LocalDateTime showDate, Double price) {
        return eventDao.createShowtime(eventId, venueId, showDate, price);
    }

    public Showtime getShowtimeById(Integer showtimeId) {
        return eventDao.getShowtimeById(showtimeId);
    }

    public void updateShowtime(Integer showtimeId, Integer venueId, LocalDateTime showDate, Double price) {
        eventDao.updateShowtime(showtimeId, venueId, showDate, price);
    }

    public void deleteShowtime(Integer showtimeId) {
        eventDao.deleteShowtime(showtimeId);
    }
}

