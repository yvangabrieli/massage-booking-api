-- ================================================
-- TIME SLOT MANAGEMENT SYSTEM
-- ================================================

-- Working days configuration (which days of week are available)
CREATE TABLE working_days (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    day_of_week TINYINT NOT NULL COMMENT '1=Monday, 7=Sunday',
    is_active BOOLEAN DEFAULT TRUE,
    open_time TIME NOT NULL DEFAULT '10:00:00',
    close_time TIME NOT NULL DEFAULT '20:00:00',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_day_of_week (day_of_week)
);

-- Specific date overrides (for holidays, special closures, extra openings)
CREATE TABLE schedule_exceptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exception_date DATE NOT NULL,
    is_available BOOLEAN DEFAULT FALSE,
    reason VARCHAR(255),
    open_time TIME,
    close_time TIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_exception_date (exception_date)
);

-- Pre-generated time slots for faster queries
CREATE TABLE time_slots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    slot_date DATE NOT NULL,
    slot_time TIME NOT NULL,
    slot_datetime DATETIME NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    is_blocked BOOLEAN DEFAULT FALSE,
    block_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_slot_datetime (slot_datetime),
    INDEX idx_slot_date (slot_date),
    INDEX idx_availability (is_available, is_blocked)
);

-- Insert your working days (Thu=4, Fri=5, Sat=6, Sun=7)
INSERT INTO working_days (day_of_week, is_active, open_time, close_time) VALUES
(4, TRUE, '10:00:00', '20:00:00'),  -- Thursday
(5, TRUE, '10:00:00', '20:00:00'),  -- Friday
(6, TRUE, '10:00:00', '20:00:00'),  -- Saturday
(7, TRUE, '10:00:00', '20:00:00');  -- Sunday

-- Monday, Tuesday, Wednesday are NOT inserted (or is_active=FALSE)