
ALTER TABLE Booking ADD COLUMN IF NOT EXISTS version INT DEFAULT 1;

CREATE OR REPLACE FUNCTION update_booking_optimistic(
    p_booking_id INT,
    p_old_version INT,
    p_new_payment_status VARCHAR(20)
) RETURNS JSON AS $$
DECLARE
    v_current_version INT;
BEGIN
    SELECT version INTO v_current_version
    FROM Booking
    WHERE booking_id = p_booking_id
    FOR UPDATE;  
    
    IF v_current_version != p_old_version THEN
        RAISE EXCEPTION 'Booking was modified by another transaction';
    END IF;
    
    UPDATE Booking
    SET payment_status = p_new_payment_status,
        version = version + 1
    WHERE booking_id = p_booking_id;
    
    RETURN json_build_object('status', 'success', 'new_version', v_current_version + 1);
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION lock_showtime_seats(p_showtime_id INT) RETURNS BOOLEAN AS $$
DECLARE
    v_lock_acquired BOOLEAN;
BEGIN
    v_lock_acquired := pg_try_advisory_xact_lock(p_showtime_id);
    
    IF NOT v_lock_acquired THEN
        RAISE EXCEPTION 'Showtime is currently being processed by another user';
    END IF;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION safe_booking_lock(
    p_showtime_id INT,
    p_seat_ids INT[]
) RETURNS BOOLEAN AS $$
BEGIN
    PERFORM 1 FROM Showtime WHERE showtime_id = p_showtime_id FOR UPDATE;
    
    PERFORM 1 FROM Seat_Status
    WHERE showtime_id = p_showtime_id
      AND seat_id = ANY(p_seat_ids)
    ORDER BY seat_id
    FOR UPDATE;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION select_seats_with_timeout(
    p_showtime_id INT,
    p_seat_ids INT[],
    p_timeout_seconds INT DEFAULT 30
) RETURNS JSON AS $$
DECLARE
    v_seat_id INT;
    v_start_time TIMESTAMP;
    v_lock_acquired BOOLEAN;
BEGIN
    v_start_time := CURRENT_TIMESTAMP;
    
    PERFORM set_config('lock_timeout', (p_timeout_seconds * 1000)::text, false);
    
    BEGIN
        PERFORM 1 FROM Showtime WHERE showtime_id = p_showtime_id FOR UPDATE NOWAIT;
        
        FOREACH v_seat_id IN ARRAY p_seat_ids LOOP
            PERFORM 1 FROM Seat_Status
            WHERE showtime_id = p_showtime_id
              AND seat_id = v_seat_id
              AND status = 'available'
            FOR UPDATE NOWAIT;
        END LOOP;
        
        v_lock_acquired := TRUE;
    EXCEPTION
        WHEN lock_not_available THEN
            v_lock_acquired := FALSE;
    END;
    
    IF NOT v_lock_acquired THEN
        RETURN json_build_object(
            'status', 'error',
            'message', 'Seats are currently being booked by another user'
        );
    END IF;
    
    RETURN json_build_object('status', 'success', 'seats_locked', p_seat_ids);
END;
$$ LANGUAGE plpgsql;


