-- ============================================
-- CONCURRENCY CONTROL MECHANISMS
-- ============================================

-- Row-level locking is already implemented in transactions.sql
-- This file contains additional concurrency control examples

-- 1. Pessimistic Locking (Row-level locks)
-- Example: Lock seats during booking selection
-- This is done using SELECT ... FOR UPDATE in transactions.sql

-- 2. Optimistic Locking using version column (if needed)
-- Add version column to Booking table for optimistic locking
ALTER TABLE Booking ADD COLUMN IF NOT EXISTS version INT DEFAULT 1;

-- Function to update booking with optimistic locking
CREATE OR REPLACE FUNCTION update_booking_optimistic(
    p_booking_id INT,
    p_old_version INT,
    p_new_payment_status VARCHAR(20)
) RETURNS JSON AS $$
DECLARE
    v_current_version INT;
BEGIN
    -- Check version
    SELECT version INTO v_current_version
    FROM Booking
    WHERE booking_id = p_booking_id
    FOR UPDATE;  -- Lock row
    
    IF v_current_version != p_old_version THEN
        RAISE EXCEPTION 'Booking was modified by another transaction';
    END IF;
    
    -- Update with version increment
    UPDATE Booking
    SET payment_status = p_new_payment_status,
        version = version + 1
    WHERE booking_id = p_booking_id;
    
    RETURN json_build_object('status', 'success', 'new_version', v_current_version + 1);
END;
$$ LANGUAGE plpgsql;

-- 3. Advisory Locks (PostgreSQL specific)
-- Function to lock a showtime for seat selection
CREATE OR REPLACE FUNCTION lock_showtime_seats(p_showtime_id INT) RETURNS BOOLEAN AS $$
DECLARE
    v_lock_acquired BOOLEAN;
BEGIN
    -- Try to acquire advisory lock (non-blocking)
    v_lock_acquired := pg_try_advisory_xact_lock(p_showtime_id);
    
    IF NOT v_lock_acquired THEN
        RAISE EXCEPTION 'Showtime is currently being processed by another user';
    END IF;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- 4. Deadlock Prevention
-- Always lock resources in the same order
-- Example: Lock showtime first, then seats
CREATE OR REPLACE FUNCTION safe_booking_lock(
    p_showtime_id INT,
    p_seat_ids INT[]
) RETURNS BOOLEAN AS $$
BEGIN
    -- Lock showtime first (lower ID first to prevent deadlocks)
    PERFORM 1 FROM Showtime WHERE showtime_id = p_showtime_id FOR UPDATE;
    
    -- Then lock seats in sorted order
    PERFORM 1 FROM Seat_Status
    WHERE showtime_id = p_showtime_id
      AND seat_id = ANY(p_seat_ids)
    ORDER BY seat_id
    FOR UPDATE;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- 5. Isolation Level Examples
-- Set isolation level for specific transactions
-- READ COMMITTED (default): Each query sees committed data
-- REPEATABLE READ: Consistent snapshot for entire transaction
-- SERIALIZABLE: Highest isolation, prevents phantom reads

-- Example: Use SERIALIZABLE for critical booking operations
-- BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE;
-- SELECT create_booking(1, 1, ARRAY[1, 2], 'CreditCard');
-- COMMIT;

-- 6. Lock Timeout Configuration
-- Set lock timeout to prevent indefinite waiting
-- SET lock_timeout = '5s';  -- Wait max 5 seconds for lock

-- 7. Check for deadlocks
-- PostgreSQL automatically detects and resolves deadlocks
-- One transaction will be rolled back with error: "deadlock detected"

-- 8. Concurrent seat selection with timeout
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
    
    -- Set lock timeout
    PERFORM set_config('lock_timeout', (p_timeout_seconds * 1000)::text, false);
    
    -- Try to acquire locks
    BEGIN
        -- Lock showtime
        PERFORM 1 FROM Showtime WHERE showtime_id = p_showtime_id FOR UPDATE NOWAIT;
        
        -- Lock seats
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

-- 9. Monitor active locks (for debugging)
-- Query to see current locks:
-- SELECT * FROM pg_locks WHERE NOT granted;

-- 10. Application-level concurrency: Use database-level locks
-- The create_booking function already uses FOR UPDATE for row-level locking
-- This ensures only one transaction can book a seat at a time

