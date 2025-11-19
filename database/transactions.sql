-- ============================================
-- TRANSACTION MANAGEMENT AND ACID PROPERTIES
-- ============================================

-- Function to generate alphanumeric booking code
CREATE OR REPLACE FUNCTION generate_booking_code() RETURNS VARCHAR(16) AS $$
DECLARE
    chars VARCHAR(62) := 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz';
    result VARCHAR(16) := '';
    i INT;
BEGIN
    FOR i IN 1..16 LOOP
        result := result || substr(chars, floor(random() * 62 + 1)::int, 1);
    END LOOP;
    
    -- Ensure uniqueness
    WHILE EXISTS (SELECT 1 FROM Booking WHERE booking_code = result) LOOP
        result := '';
        FOR i IN 1..16 LOOP
            result := result || substr(chars, floor(random() * 62 + 1)::int, 1);
        END LOOP;
    END LOOP;
    
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Stored Procedure: Complete booking transaction
-- Implements ACID properties:
-- ATOMICITY: All or nothing
-- CONSISTENCY: Seat status and booking must be consistent
-- ISOLATION: Uses row-level locking
-- DURABILITY: Committed changes persist
CREATE OR REPLACE FUNCTION create_booking(
    p_user_id INT,
    p_showtime_id INT,
    p_seat_ids INT[],
    p_payment_method VARCHAR(20)
) RETURNS JSON AS $$
DECLARE
    v_booking_id INT;
    v_booking_code VARCHAR(16);
    v_total_amount DECIMAL(10,2) := 0;
    v_seat_price DECIMAL(10,2);
    v_seat_id INT;
    v_seat_count INT;
    v_available_count INT;
BEGIN
    -- Start transaction (implicit in function)
    
    -- Validate: Check if all seats are available
    SELECT COUNT(*) INTO v_seat_count FROM unnest(p_seat_ids);
    SELECT COUNT(*) INTO v_available_count
    FROM Seat_Status
    WHERE showtime_id = p_showtime_id
      AND seat_id = ANY(p_seat_ids)
      AND status = 'available'
      FOR UPDATE;  -- Row-level lock for concurrency control
    
    IF v_available_count < v_seat_count THEN
        RAISE EXCEPTION 'Some seats are not available';
    END IF;
    
    -- Calculate total amount
    SELECT price INTO v_seat_price FROM Showtime WHERE showtime_id = p_showtime_id;
    v_total_amount := v_seat_price * v_seat_count;
    
    -- Generate unique booking code
    v_booking_code := generate_booking_code();
    
    -- Create booking
    INSERT INTO Booking (user_id, total_amount, booking_code, payment_status)
    VALUES (p_user_id, v_total_amount, v_booking_code, 'pending')
    RETURNING booking_id INTO v_booking_id;
    
    -- Book seats and update status
    FOREACH v_seat_id IN ARRAY p_seat_ids LOOP
        -- Insert booked seat
        INSERT INTO Booked_Seats (booking_id, showtime_id, seat_id, price_paid)
        VALUES (v_booking_id, p_showtime_id, v_seat_id, v_seat_price);
        
        -- Update seat status to booked
        UPDATE Seat_Status
        SET status = 'booked'
        WHERE showtime_id = p_showtime_id AND seat_id = v_seat_id;
    END LOOP;
    
    -- Create payment record
    INSERT INTO Payment (booking_id, amount, method)
    VALUES (v_booking_id, v_total_amount, p_payment_method);
    
    -- Update booking payment status
    UPDATE Booking
    SET payment_status = 'completed'
    WHERE booking_id = v_booking_id;
    
    -- Return booking details
    RETURN json_build_object(
        'booking_id', v_booking_id,
        'booking_code', v_booking_code,
        'total_amount', v_total_amount,
        'status', 'success'
    );
    
EXCEPTION
    WHEN OTHERS THEN
        -- Rollback on error (automatic in function)
        RAISE;
END;
$$ LANGUAGE plpgsql;

-- Example: Transaction to cancel booking (future scope)
CREATE OR REPLACE FUNCTION cancel_booking(p_booking_id INT) RETURNS JSON AS $$
DECLARE
    v_showtime_id INT;
    v_seat_id INT;
BEGIN
    -- Get showtime and seats for this booking
    FOR v_showtime_id, v_seat_id IN
        SELECT bs.showtime_id, bs.seat_id
        FROM Booked_Seats bs
        WHERE bs.booking_id = p_booking_id
    LOOP
        -- Release seats (set back to available)
        UPDATE Seat_Status
        SET status = 'available'
        WHERE showtime_id = v_showtime_id AND seat_id = v_seat_id;
    END LOOP;
    
    -- Delete booked seats
    DELETE FROM Booked_Seats WHERE booking_id = p_booking_id;
    
    -- Delete payment
    DELETE FROM Payment WHERE booking_id = p_booking_id;
    
    -- Delete booking
    DELETE FROM Booking WHERE booking_id = p_booking_id;
    
    RETURN json_build_object('status', 'cancelled', 'booking_id', p_booking_id);
END;
$$ LANGUAGE plpgsql;

-- Example transaction: Block seats temporarily (for seat selection)
-- Uses transaction with timeout simulation
CREATE OR REPLACE FUNCTION block_seats(
    p_showtime_id INT,
    p_seat_ids INT[]
) RETURNS JSON AS $$
DECLARE
    v_seat_id INT;
    v_blocked_count INT := 0;
BEGIN
    FOREACH v_seat_id IN ARRAY p_seat_ids LOOP
        -- Only block if available
        UPDATE Seat_Status
        SET status = 'blocked'
        WHERE showtime_id = p_showtime_id
          AND seat_id = v_seat_id
          AND status = 'available';
        
        IF FOUND THEN
            v_blocked_count := v_blocked_count + 1;
        END IF;
    END LOOP;
    
    RETURN json_build_object(
        'status', 'success',
        'blocked_count', v_blocked_count
    );
END;
$$ LANGUAGE plpgsql;

-- Example: Transaction to unblock seats
CREATE OR REPLACE FUNCTION unblock_seats(
    p_showtime_id INT,
    p_seat_ids INT[]
) RETURNS JSON AS $$
BEGIN
    UPDATE Seat_Status
    SET status = 'available'
    WHERE showtime_id = p_showtime_id
      AND seat_id = ANY(p_seat_ids)
      AND status = 'blocked';
    
    RETURN json_build_object('status', 'success');
END;
$$ LANGUAGE plpgsql;

-- Example transaction usage (for reference):
-- BEGIN;
-- SELECT create_booking(1, 1, ARRAY[1, 2], 'CreditCard');
-- COMMIT;

