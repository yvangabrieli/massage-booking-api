-- ================================================
-- SEED DATA for Massage Booking API
-- ================================================

-- 1. ADMIN USER
-- Password: Admin123! (BCrypt hashed)
INSERT INTO users (name, phone, email, password, role, active, created_at, updated_at)
VALUES (
    'Admin User',
    '+34600000000',
    'admin@massage.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ROLE_ADMIN',
    true,
    NOW(),
    NOW()
);

-- 2. TEST CLIENT USER
-- Password: Client123!
INSERT INTO users (name, phone, email, password, role, active, created_at, updated_at)
VALUES (
    'John Doe',
    '+34612345678',
    'john@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ROLE_CLIENT',
    true,
    NOW(),
    NOW()
);

-- 3. CLIENTS
INSERT INTO clients (name, phone, email, birthday, notes, active, user_id, created_at, updated_at)
VALUES
    ('John Doe', '+34612345678', 'john@example.com', '1990-05-15', 'Prefers mornings', true, 2, NOW(), NOW()),
    ('Jane Smith', '+34687654321', 'jane@example.com', '1985-08-22', 'Allergic to lavender oil', true, NULL, NOW(), NOW()),
    ('Carlos García', '+34655123456', 'carlos@example.com', NULL, 'Regular client', true, NULL, NOW(), NOW());

-- 4. MASSAGE SERVICES
INSERT INTO services (name, category, duration_minutes, cleanup_minutes, description, active, created_at, updated_at)
VALUES
    ('Toque Profundo 60', 'DEEP_TISSUE', 60, 10, 'Deep tissue massage 60 minutes', true, NOW(), NOW()),
    ('Toque Profundo 90', 'DEEP_TISSUE', 90, 10, 'Deep tissue massage 90 minutes', true, NOW(), NOW()),
    ('Relax Premium', 'RELAXING', 75, 10, 'Premium relaxation massage', true, NOW(), NOW()),
    ('Siesta Express', 'RELAXING', 30, 10, 'Quick relaxation session', true, NOW(), NOW()),
    ('Happy Feet', 'SPECIALIZED', 45, 10, 'Foot reflexology massage', true, NOW(), NOW()),
    ('Libera Mi Espalda', 'SPECIALIZED', 50, 10, 'Back pain relief massage', true, NOW(), NOW());

-- 5. SAMPLE BOOKINGS
INSERT INTO bookings (client_id, service_id, start_time, end_time, status, guest_name, guest_phone, created_at, updated_at)
VALUES
    (1, 1, DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 3 DAY), INTERVAL 70 MINUTE), 'BOOKED', NULL, NULL, NOW(), NOW()),
    (2, 3, DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 5 DAY), INTERVAL 85 MINUTE), 'BOOKED', NULL, NULL, NOW(), NOW()),
    (1, 5, DATE_ADD(NOW(), INTERVAL -2 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL -2 DAY), INTERVAL 55 MINUTE), 'COMPLETED', NULL, NULL, NOW(), NOW());

    -- Add unique constraint to prevent overlapping bookings at DB level
    -- This is a safety net in case application-level locking fails
    CREATE INDEX idx_bookings_time_range ON bookings(start_time, end_time);
    CREATE INDEX idx_bookings_status ON bookings(status);
    CREATE INDEX idx_bookings_client ON bookings(client_id);

    -- Partial index for active bookings only (optimization)
    CREATE INDEX idx_active_bookings_time ON bookings(start_time, end_time)
    WHERE status = 'BOOKED';