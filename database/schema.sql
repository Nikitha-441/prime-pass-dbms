CREATE TABLE Users (
    user_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(10) NOT NULL DEFAULT 'user'
);
CREATE TABLE Organizer (
    organizer_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15)
);
CREATE TABLE Category (
    category_id SERIAL PRIMARY KEY,
    category_name VARCHAR(100) UNIQUE NOT NULL
);
CREATE TABLE Venue (
    venue_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    location VARCHAR(200),
    total_seats INT NOT NULL CHECK (total_seats > 0)
);
CREATE TABLE Event (
    event_id SERIAL PRIMARY KEY,
    organizer_id INT NOT NULL,
    category_id INT NOT NULL,
    event_name VARCHAR(100) NOT NULL,
    description TEXT,
    base_price DECIMAL(10,2) NOT NULL CHECK (base_price >= 0),

    FOREIGN KEY (organizer_id) REFERENCES Organizer(organizer_id) ON DELETE CASCADE ON UPDATE RESTRICT,
    FOREIGN KEY (category_id) REFERENCES Category(category_id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE TABLE Showtime (
    showtime_id SERIAL PRIMARY KEY,
    event_id INT NOT NULL,
    venue_id INT NOT NULL,
    show_date TIMESTAMP NOT NULL,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),

    FOREIGN KEY (event_id) REFERENCES Event(event_id) ON DELETE CASCADE,
    FOREIGN KEY (venue_id) REFERENCES Venue(venue_id) ON DELETE CASCADE
);
CREATE TABLE Seat (
    seat_id SERIAL PRIMARY KEY,
    venue_id INT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,

    UNIQUE (venue_id, seat_number),

    FOREIGN KEY (venue_id) REFERENCES Venue(venue_id) ON DELETE CASCADE
);
CREATE TABLE Seat_Status (
    seat_status_id SERIAL PRIMARY KEY,
    showtime_id INT NOT NULL,
    seat_id INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'available',

    UNIQUE (showtime_id, seat_id),

    FOREIGN KEY (showtime_id) REFERENCES Showtime(showtime_id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES Seat(seat_id) ON DELETE CASCADE
);
CREATE TABLE Booking (
    booking_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    booking_code VARCHAR(16) UNIQUE,
    payment_status VARCHAR(20) DEFAULT 'pending',

    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);
CREATE TABLE Booked_Seats (
    booked_seat_id SERIAL PRIMARY KEY,
    booking_id INT NOT NULL,
    showtime_id INT NOT NULL,
    seat_id INT NOT NULL,
    price_paid DECIMAL(10,2) NOT NULL,

    UNIQUE (showtime_id, seat_id),

    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (showtime_id) REFERENCES Showtime(showtime_id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES Seat(seat_id) ON DELETE CASCADE
);
CREATE TABLE Payment (
    payment_id SERIAL PRIMARY KEY,
    booking_id INT UNIQUE NOT NULL,
    amount DECIMAL(10,2) NOT NULL CHECK (amount >= 0),
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    method VARCHAR(20) NOT NULL CHECK (method IN ('CreditCard','DebitCard','UPI','Cash')),

    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE
);

-- SAMPLE DATA
INSERT INTO Users (name, email, password, role) VALUES
('Rishitha', 'rishitha@primepass.com', '$2b$10$u1', 'user'),
('Arjun', 'arjun@primepass.com', '$2b$10$u2', 'admin');

INSERT INTO Organizer (name, contact_email, phone) VALUES
('CineWorld', 'info@cineworld.com', '9876543210'),
('LiveArena', 'support@livearena.com', '8765432109');

INSERT INTO Category (category_name) VALUES
('Movie'),
('Concert'),
('Drama');

INSERT INTO Venue (name, location, total_seats) VALUES
('Grand Hall', 'Hyderabad', 500),
('Sky Theatre', 'Bangalore', 300);

-- Generate a basic seat layout (A1–A10, B1–B10, C1–C10, D1–D10, E1–E10) for each venue
DO $$
DECLARE
    v_venue_id INT;
    row_label CHAR(1);
    seat_num INT;
BEGIN
    FOR v_venue_id IN SELECT venue_id FROM Venue LOOP
        FOR row_label IN 'A','B','C','D','E' LOOP
            FOR seat_num IN 1..10 LOOP
                INSERT INTO Seat (venue_id, seat_number)
                VALUES (v_venue_id, row_label || seat_num)
                ON CONFLICT (venue_id, seat_number) DO NOTHING;
            END LOOP;
        END LOOP;
    END LOOP;
END $$;

INSERT INTO Event (organizer_id, category_id, event_name, description, base_price) VALUES
(1, 1, 'Avengers Premiere', 'Special movie screening', 300.00),
(2, 2, 'Arijit Singh Live', 'Concert by Arijit Singh', 1500.00);

INSERT INTO Showtime (event_id, venue_id, show_date, price) VALUES
(1, 1, '2025-12-01 18:00:00', 300.00),
(2, 2, '2025-12-05 19:00:00', 1500.00);

-- Initialize seat status for sample showtimes: mark all seats as available
INSERT INTO Seat_Status (showtime_id, seat_id, status)
SELECT st.showtime_id, s.seat_id, 'available'
FROM Showtime st
JOIN Venue v ON st.venue_id = v.venue_id
JOIN Seat s ON v.venue_id = s.venue_id
ON CONFLICT (showtime_id, seat_id) DO NOTHING;
