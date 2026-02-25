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
-- Password: Admin123! (same hash for testing)
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

-- 3. CLIENTS (user_id=2 links John Doe's client record to his user account)
INSERT INTO clients (name, phone, email, birthday, notes, active, user_id, created_at, updated_at)
VALUES
    ('John Doe', '+34612345678', 'john@example.com', '1990-05-15', 'Prefers mornings', true, 2, NOW(), NOW()),
    ('Jane Smith', '+34687654321', 'jane@example.com', '1985-08-22', 'Allergic to lavender oil', true, NULL, NOW(), NOW()),
    ('Carlos García', '+34655123456', 'carlos@example.com', NULL, 'Regular client', true, NULL, NOW(), NOW());

-- 4. MASSAGE SERVICES
-- FIX #3: Added price column (was missing — causes NOT NULL constraint violation)
INSERT INTO services (name, category, duration_minutes, cleanup_minutes, price, description, active, created_at, updated_at)
VALUES
    ('Toque Profundo 60', 'DEEP_TISSUE', 60, 10, 65.00, 'Deep tissue massage 60 minutes', true, NOW(), NOW()),
    ('Toque Profundo 90', 'DEEP_TISSUE', 90, 10, 85.00, 'Deep tissue massage 90 minutes', true, NOW(), NOW()),
    ('Relax Premium',     'RELAXING',    75, 10, 70.00, 'Premium relaxation massage', true, NOW(), NOW()),
    ('Siesta Express',    'RELAXING',    40, 10, 43.00, 'Quick relaxation session', true, NOW(), NOW()),
    ('Happy Feet',        'SPECIALIZED', 30, 10, 25.00, 'Foot reflexology massage', true, NOW(), NOW()),
    ('Libera Mi Espalda', 'SPECIALIZED', 50, 10, 50.00, 'Back pain relief massage', true, NOW(), NOW());

-- 5. SAMPLE BOOKINGS
INSERT INTO bookings (client_id, service_id, start_time, end_time, status, guest_name, guest_phone, created_at, updated_at)
VALUES
    (1, 1, DATE_ADD(NOW(), INTERVAL 3 DAY),  DATE_ADD(DATE_ADD(NOW(), INTERVAL 3 DAY), INTERVAL 70 MINUTE),  'BOOKED',    NULL, NULL, NOW(), NOW()),
    (2, 3, DATE_ADD(NOW(), INTERVAL 5 DAY),  DATE_ADD(DATE_ADD(NOW(), INTERVAL 5 DAY), INTERVAL 85 MINUTE),  'BOOKED',    NULL, NULL, NOW(), NOW()),
    (1, 5, DATE_ADD(NOW(), INTERVAL -2 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL -2 DAY), INTERVAL 55 MINUTE), 'COMPLETED', NULL, NULL, NOW(), NOW());

-- FIX #4: Removed the partial index (WHERE status = 'BOOKED') — MySQL does NOT support partial indexes.
-- Regular indexes only:
CREATE INDEX IF NOT EXISTS idx_bookings_time_range ON bookings(start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_bookings_status     ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_bookings_client     ON bookings(client_id);
