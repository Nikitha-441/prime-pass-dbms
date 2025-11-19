-- Function to authenticate user
CREATE OR REPLACE FUNCTION authenticate_user(
    p_email VARCHAR(100),
    p_password VARCHAR(255)
) RETURNS JSON AS $$
DECLARE
    v_user_id INT;
    v_name VARCHAR(100);
    v_role VARCHAR(10);
    v_stored_password VARCHAR(255);
BEGIN
    -- Get user credentials
    SELECT user_id, name, role, password
    INTO v_user_id, v_name, v_role, v_stored_password
    FROM Users
    WHERE email = p_email;
    
    -- Check if user exists
    IF v_user_id IS NULL THEN
        RETURN json_build_object(
            'success', false,
            'message', 'Invalid email or password'
        );
    END IF;
    
    
    IF v_stored_password != p_password THEN
        RETURN json_build_object(
            'success', false,
            'message', 'Invalid email or password'
        );
    END IF;
    
    -- Return user info (without password)
    RETURN json_build_object(
        'success', true,
        'user_id', v_user_id,
        'name', v_name,
        'email', p_email,
        'role', v_role
    );
END;
$$ LANGUAGE plpgsql;

-- Function to register new user
CREATE OR REPLACE FUNCTION register_user(
    p_name VARCHAR(100),
    p_email VARCHAR(100),
    p_password VARCHAR(255)
) RETURNS JSON AS $$
DECLARE
    v_user_id INT;
    v_existing_user INT;
BEGIN
    -- Check if email already exists
    SELECT user_id INTO v_existing_user
    FROM Users
    WHERE email = p_email;
    
    IF v_existing_user IS NOT NULL THEN
        RETURN json_build_object(
            'success', false,
            'message', 'Email already registered'
        );
    END IF;
    
    -- Insert new user (password should be hashed in application layer)
    INSERT INTO Users (name, email, password, role)
    VALUES (p_name, p_email, p_password, 'user')
    RETURNING user_id INTO v_user_id;
    
    RETURN json_build_object(
        'success', true,
        'user_id', v_user_id,
        'message', 'User registered successfully'
    );
END;
$$ LANGUAGE plpgsql;

-- Function to check if user has admin role
CREATE OR REPLACE FUNCTION is_admin(p_user_id INT) RETURNS BOOLEAN AS $$
DECLARE
    v_role VARCHAR(10);
BEGIN
    SELECT role INTO v_role
    FROM Users
    WHERE user_id = p_user_id;
    
    RETURN COALESCE(v_role = 'admin', false);
END;
$$ LANGUAGE plpgsql;

-- Function to get user by ID (for token validation)
CREATE OR REPLACE FUNCTION get_user_by_id(p_user_id INT) RETURNS JSON AS $$
DECLARE
    v_user RECORD;
BEGIN
    SELECT user_id, name, email, role
    INTO v_user
    FROM Users
    WHERE user_id = p_user_id;
    
    IF v_user IS NULL THEN
        RETURN json_build_object('success', false, 'message', 'User not found');
    END IF;
    
    RETURN json_build_object(
        'success', true,
        'user_id', v_user.user_id,
        'name', v_user.name,
        'email', v_user.email,
        'role', v_user.role
    );
END;
$$ LANGUAGE plpgsql;

CREATE INDEX IF NOT EXISTS idx_users_email ON Users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON Users(role);

CREATE OR REPLACE FUNCTION update_password(
    p_user_id INT,
    p_old_password VARCHAR(255),
    p_new_password VARCHAR(255)
) RETURNS JSON AS $$
DECLARE
    v_current_password VARCHAR(255);
BEGIN
    -- Get current password
    SELECT password INTO v_current_password
    FROM Users
    WHERE user_id = p_user_id;
    
    IF v_current_password IS NULL THEN
        RETURN json_build_object('success', false, 'message', 'User not found');
    END IF;
    
    -- Verify old password
    IF v_current_password != p_old_password THEN
        RETURN json_build_object('success', false, 'message', 'Incorrect current password');
    END IF;
    
    -- Update password
    UPDATE Users
    SET password = p_new_password
    WHERE user_id = p_user_id;
    
    RETURN json_build_object('success', true, 'message', 'Password updated successfully');
END;
$$ LANGUAGE plpgsql;

]
CREATE TABLE IF NOT EXISTS login_attempts (
    attempt_id SERIAL PRIMARY KEY,
    email VARCHAR(100),
    attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN,
    ip_address VARCHAR(45)
);

-- Function to log login attempts
CREATE OR REPLACE FUNCTION log_login_attempt(
    p_email VARCHAR(100),
    p_success BOOLEAN,
    p_ip_address VARCHAR(45) DEFAULT NULL
) RETURNS VOID AS $$
BEGIN
    INSERT INTO login_attempts (email, success, ip_address)
    VALUES (p_email, p_success, p_ip_address);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION authenticate_user_with_logging(
    p_email VARCHAR(100),
    p_password VARCHAR(255),
    p_ip_address VARCHAR(45) DEFAULT NULL
) RETURNS JSON AS $$
DECLARE
    v_result JSON;
BEGIN
    v_result := authenticate_user(p_email, p_password);
    
    PERFORM log_login_attempt(
        p_email,
        (v_result->>'success')::boolean,
        p_ip_address
    );
    
    RETURN v_result;
END;
$$ LANGUAGE plpgsql;

