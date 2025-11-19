-- ============================================
-- ROLE-BASED VIEWS FOR USER AND ADMIN
-- ============================================

-- View for Users: Shows available events with basic details
CREATE OR REPLACE VIEW user_events_view AS
SELECT 
    e.event_id,
    e.event_name,
    e.description,
    e.base_price,
    e.organizer_id,
    e.category_id,
    c.category_name,
    o.name AS organizer_name,
    COUNT(DISTINCT st.showtime_id) AS showtime_count,
    MIN(st.show_date) AS next_show_date,
    MIN(v.name) AS next_venue
FROM Event e
JOIN Category c ON e.category_id = c.category_id
JOIN Organizer o ON e.organizer_id = o.organizer_id
LEFT JOIN Showtime st ON e.event_id = st.event_id
LEFT JOIN Venue v ON st.venue_id = v.venue_id
GROUP BY e.event_id, e.event_name, e.description, e.base_price, e.organizer_id, e.category_id, c.category_name, o.name;

-- View for Users: Shows event details with showtimes
CREATE OR REPLACE VIEW user_event_details_view AS
SELECT 
    e.event_id,
    e.event_name,
    e.description,
    e.base_price,
    c.category_name,
    o.name AS organizer_name,
    o.contact_email AS organizer_email,
    st.showtime_id,
    st.show_date,
    st.price AS showtime_price,
    v.name AS venue_name,
    v.location AS venue_location,
    v.total_seats
FROM Event e
JOIN Category c ON e.category_id = c.category_id
JOIN Organizer o ON e.organizer_id = o.organizer_id
JOIN Showtime st ON e.event_id = st.event_id
JOIN Venue v ON st.venue_id = v.venue_id;

-- View for Users: Shows booking history with event details
CREATE OR REPLACE VIEW user_booking_history_view AS
SELECT 
    b.booking_id,
    b.booking_code,
    b.booking_time,
    b.total_amount,
    b.payment_status,
    b.user_id,
    e.event_name,
    c.category_name,
    st.show_date,
    v.name AS venue_name,
    STRING_AGG(s.seat_number, ', ' ORDER BY s.seat_number) AS seats,
    COUNT(bs.seat_id) AS seat_count
FROM Booking b
JOIN Users u ON b.user_id = u.user_id
JOIN Booked_Seats bs ON b.booking_id = bs.booking_id
JOIN Showtime st ON bs.showtime_id = st.showtime_id
JOIN Event e ON st.event_id = e.event_id
JOIN Category c ON e.category_id = c.category_id
JOIN Venue v ON st.venue_id = v.venue_id
JOIN Seat s ON bs.seat_id = s.seat_id
GROUP BY b.booking_id, b.booking_code, b.booking_time, b.total_amount, 
         b.payment_status, b.user_id, e.event_name, c.category_name, st.show_date, v.name;

-- View for Admin: Shows all events with management details
CREATE OR REPLACE VIEW admin_events_view AS
SELECT 
    e.event_id,
    e.event_name,
    e.description,
    e.base_price,
    e.organizer_id,
    e.category_id,
    c.category_name,
    o.name AS organizer_name,
    o.contact_email AS organizer_email,
    COUNT(DISTINCT st.showtime_id) AS showtime_count,
    COUNT(DISTINCT b.booking_id) AS booking_count,
    COALESCE(SUM(b.total_amount), 0) AS total_revenue,
    MIN(st.show_date) AS next_show_date,
    MIN(v.name) AS next_venue
FROM Event e
JOIN Category c ON e.category_id = c.category_id
JOIN Organizer o ON e.organizer_id = o.organizer_id
LEFT JOIN Showtime st ON e.event_id = st.event_id
LEFT JOIN Venue v ON st.venue_id = v.venue_id
LEFT JOIN Booked_Seats bs ON st.showtime_id = bs.showtime_id
LEFT JOIN Booking b ON bs.booking_id = b.booking_id
GROUP BY e.event_id, e.event_name, e.description, e.base_price, e.organizer_id, e.category_id,
         c.category_name, o.name, o.contact_email;

-- View for Admin: Shows all bookings with user and event details
CREATE OR REPLACE VIEW admin_bookings_view AS
SELECT 
    b.booking_id,
    b.booking_code,
    b.booking_time,
    b.total_amount,
    b.payment_status,
    b.user_id,
    u.name AS user_name,
    u.email AS user_email,
    e.event_name,
    c.category_name,
    st.show_date,
    v.name AS venue_name,
    COUNT(bs.seat_id) AS seat_count,
    STRING_AGG(s.seat_number, ', ' ORDER BY s.seat_number) AS seats
FROM Booking b
JOIN Users u ON b.user_id = u.user_id
JOIN Booked_Seats bs ON b.booking_id = bs.booking_id
JOIN Showtime st ON bs.showtime_id = st.showtime_id
JOIN Event e ON st.event_id = e.event_id
JOIN Category c ON e.category_id = c.category_id
JOIN Venue v ON st.venue_id = v.venue_id
JOIN Seat s ON bs.seat_id = s.seat_id
GROUP BY b.booking_id, b.booking_code, b.booking_time, b.total_amount, 
         b.payment_status, b.user_id, u.name, u.email, e.event_name, c.category_name, 
         st.show_date, v.name;

-- View for seat availability per showtime
CREATE OR REPLACE VIEW seat_availability_view AS
SELECT 
    st.showtime_id,
    st.show_date,
    e.event_name,
    v.name AS venue_name,
    COUNT(DISTINCT s.seat_id) AS total_seats,
    COUNT(DISTINCT CASE WHEN ss.status = 'available' THEN s.seat_id END) AS available_seats,
    COUNT(DISTINCT CASE WHEN ss.status = 'booked' THEN s.seat_id END) AS booked_seats,
    COUNT(DISTINCT CASE WHEN ss.status = 'blocked' THEN s.seat_id END) AS blocked_seats
FROM Showtime st
JOIN Event e ON st.event_id = e.event_id
JOIN Venue v ON st.venue_id = v.venue_id
LEFT JOIN Seat s ON v.venue_id = s.venue_id
LEFT JOIN Seat_Status ss ON st.showtime_id = ss.showtime_id AND s.seat_id = ss.seat_id
GROUP BY st.showtime_id, st.show_date, e.event_name, v.name;

