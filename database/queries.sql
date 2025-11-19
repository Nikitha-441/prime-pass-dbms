
SELECT 
    e.event_id,
    e.event_name,
    c.category_name,
    st.showtime_id,
    st.show_date,
    st.price,
    v.name AS venue_name,
    COUNT(DISTINCT CASE WHEN ss.status = 'available' THEN s.seat_id END) AS available_seats
FROM Event e
JOIN Category c ON e.category_id = c.category_id
JOIN Showtime st ON e.event_id = st.event_id
JOIN Venue v ON st.venue_id = v.venue_id
LEFT JOIN Seat s ON v.venue_id = s.venue_id
LEFT JOIN Seat_Status ss ON st.showtime_id = ss.showtime_id AND s.seat_id = ss.seat_id
GROUP BY e.event_id, e.event_name, c.category_name, st.showtime_id, st.show_date, st.price, v.name
ORDER BY e.event_name, st.show_date;


SELECT 
    e.event_id,
    e.event_name,
    c.category_name,
    COUNT(DISTINCT b.booking_id) AS total_bookings,
    COUNT(DISTINCT bs.seat_id) AS total_seats_booked,
    COALESCE(SUM(b.total_amount), 0) AS total_revenue
FROM Event e
JOIN Category c ON e.category_id = c.category_id
LEFT JOIN Showtime st ON e.event_id = st.event_id
LEFT JOIN Booked_Seats bs ON st.showtime_id = bs.showtime_id
LEFT JOIN Booking b ON bs.booking_id = b.booking_id
GROUP BY e.event_id, e.event_name, c.category_name
HAVING COUNT(DISTINCT b.booking_id) > 0
ORDER BY total_bookings DESC, total_revenue DESC;


SELECT 
    u.user_id,
    u.name,
    u.email,
    COUNT(DISTINCT b.booking_id) AS booking_count,
    SUM(b.total_amount) AS total_spent
FROM Users u
JOIN Booking b ON u.user_id = b.user_id
WHERE u.user_id IN (
    SELECT user_id 
    FROM Booking 
    GROUP BY user_id 
    HAVING COUNT(*) > 1
)
GROUP BY u.user_id, u.name, u.email
ORDER BY booking_count DESC;


SELECT 
    st.showtime_id,
    e.event_name,
    st.show_date,
    v.name AS venue_name,
    v.total_seats,
    COUNT(CASE WHEN ss.status = 'available' THEN 1 END) AS available_count,
    ROUND(COUNT(CASE WHEN ss.status = 'available' THEN 1 END)::numeric / v.total_seats * 100, 2) AS availability_percent
FROM Showtime st
JOIN Event e ON st.event_id = e.event_id
JOIN Venue v ON st.venue_id = v.venue_id
LEFT JOIN Seat s ON v.venue_id = s.venue_id
LEFT JOIN Seat_Status ss ON st.showtime_id = ss.showtime_id AND s.seat_id = ss.seat_id
GROUP BY st.showtime_id, e.event_name, st.show_date, v.name, v.total_seats
HAVING COUNT(CASE WHEN ss.status = 'available' THEN 1 END)::numeric / v.total_seats < 0.20
ORDER BY availability_percent;


SELECT 
    e.event_id,
    e.event_name,
    c.category_name,
    e.base_price,
    COUNT(DISTINCT st.showtime_id) AS showtime_count
FROM Event e
JOIN Category c ON e.category_id = c.category_id
LEFT JOIN Showtime st ON e.event_id = st.event_id
LEFT JOIN Booked_Seats bs ON st.showtime_id = bs.showtime_id
WHERE bs.showtime_id IS NULL
GROUP BY e.event_id, e.event_name, c.category_name, e.base_price;


SELECT 
    c.category_name,
    COUNT(DISTINCT e.event_id) AS event_count,
    COUNT(DISTINCT b.booking_id) AS booking_count,
    COUNT(DISTINCT bs.seat_id) AS seat_count,
    COALESCE(SUM(b.total_amount), 0) AS total_revenue,
    COALESCE(AVG(b.total_amount), 0) AS avg_booking_value
FROM Category c
LEFT JOIN Event e ON c.category_id = e.category_id
LEFT JOIN Showtime st ON e.event_id = st.event_id
LEFT JOIN Booked_Seats bs ON st.showtime_id = bs.showtime_id
LEFT JOIN Booking b ON bs.booking_id = b.booking_id
GROUP BY c.category_name
ORDER BY total_revenue DESC;

SELECT 
    s.seat_id,
    s.seat_number,
    ss.status,
    CASE 
        WHEN bs.booking_id IS NOT NULL THEN b.booking_code
        ELSE NULL
    END AS booking_code
FROM Seat s
JOIN Seat_Status ss ON s.seat_id = ss.seat_id
LEFT JOIN Booked_Seats bs ON ss.showtime_id = bs.showtime_id AND ss.seat_id = bs.seat_id
LEFT JOIN Booking b ON bs.booking_id = b.booking_id
WHERE ss.showtime_id = ?  
ORDER BY s.seat_number;


SELECT 
    st.showtime_id,
    e.event_name,
    st.show_date,
    v.name AS venue_name,
    st.price,
    COUNT(CASE WHEN ss.status = 'available' THEN 1 END) AS available_seats
FROM Showtime st
JOIN Event e ON st.event_id = e.event_id
JOIN Venue v ON st.venue_id = v.venue_id
LEFT JOIN Seat s ON v.venue_id = s.venue_id
LEFT JOIN Seat_Status ss ON st.showtime_id = ss.showtime_id AND s.seat_id = ss.seat_id
WHERE st.show_date > CURRENT_TIMESTAMP
GROUP BY st.showtime_id, e.event_name, st.show_date, v.name, st.price
HAVING COUNT(CASE WHEN ss.status = 'available' THEN 1 END) > 0
ORDER BY st.show_date;


SELECT 
    o.organizer_id,
    o.name AS organizer_name,
    COUNT(DISTINCT e.event_id) AS total_events,
    COUNT(DISTINCT st.showtime_id) AS total_showtimes,
    COUNT(DISTINCT b.booking_id) AS total_bookings,
    COALESCE(SUM(b.total_amount), 0) AS total_revenue
FROM Organizer o
LEFT JOIN Event e ON o.organizer_id = e.organizer_id
LEFT JOIN Showtime st ON e.event_id = st.event_id
LEFT JOIN Booked_Seats bs ON st.showtime_id = bs.showtime_id
LEFT JOIN Booking b ON bs.booking_id = b.booking_id
GROUP BY o.organizer_id, o.name
ORDER BY total_revenue DESC;


SELECT 
    b.booking_id,
    b.booking_code,
    b.booking_time,
    b.total_amount,
    b.payment_status,
    u.name AS user_name,
    u.email AS user_email,
    p.payment_id,
    p.method AS payment_method,
    p.payment_date
FROM Booking b
JOIN Users u ON b.user_id = u.user_id
LEFT JOIN Payment p ON b.booking_id = p.booking_id
ORDER BY b.booking_time DESC;

