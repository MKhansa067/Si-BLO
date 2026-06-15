-- ============================================================
-- SI-BLO (Pro Court Rentals) - Database Schema
-- Target: PostgreSQL / H2 (in-memory)
-- ============================================================

-- Users
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(20) NOT NULL CHECK (role IN ('MEMBER', 'ADMIN')),
    membership_tier VARCHAR(100) DEFAULT 'PREMIUM MEMBER',
    avatar_url      VARCHAR(500)
);

-- Sports
CREATE TABLE sports (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL UNIQUE,
    slug            VARCHAR(100) NOT NULL UNIQUE,
    icon            VARCHAR(50),
    location_count  INTEGER DEFAULT 0,
    image_url       VARCHAR(500)
);

-- Venues
CREATE TABLE venues (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    address         VARCHAR(500),
    zone            VARCHAR(100)
);

-- Courts
CREATE TABLE courts (
    id              BIGSERIAL PRIMARY KEY,
    venue_id        BIGINT REFERENCES venues(id) ON DELETE SET NULL,
    sport_id        BIGINT REFERENCES sports(id) ON DELETE SET NULL,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    surface_type    VARCHAR(100),
    indoor          BOOLEAN DEFAULT TRUE,
    price_per_hour  INTEGER NOT NULL,
    capacity        INTEGER NOT NULL,
    rating          DOUBLE PRECISION DEFAULT 0,
    review_count    INTEGER DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'MAINTENANCE', 'INACTIVE')),
    image_url       VARCHAR(500),
    badge_label     VARCHAR(100)
);

-- Time slots
CREATE TABLE time_slots (
    id              BIGSERIAL PRIMARY KEY,
    court_id        BIGINT NOT NULL REFERENCES courts(id) ON DELETE CASCADE,
    date            DATE NOT NULL,
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'BOOKED', 'BLOCKED')),
    version         BIGINT DEFAULT 0,
    UNIQUE (court_id, date, start_time)
);

-- Bookings
CREATE TABLE bookings (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    court_id        BIGINT NOT NULL REFERENCES courts(id) ON DELETE CASCADE,
    date            DATE NOT NULL,
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,
    total_price     INTEGER NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING_PAYMENT' CHECK (status IN ('CONFIRMED', 'PENDING_PAYMENT', 'COMPLETED', 'CANCELLED', 'ACTIVE')),
    created_at      DATE DEFAULT CURRENT_DATE
);

-- Booking slot IDs (many-to-many relationship between bookings and time slots)
CREATE TABLE booking_slot_ids (
    booking_id      BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    slot_id         BIGINT NOT NULL,
    PRIMARY KEY (booking_id, slot_id)
);

-- Payments
CREATE TABLE payments (
    id              BIGSERIAL PRIMARY KEY,
    booking_id      BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    amount          INTEGER NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    method          VARCHAR(50),
    paid_at         TIMESTAMP
);

-- Indexes
CREATE INDEX idx_courts_sport_id ON courts(sport_id);
CREATE INDEX idx_courts_status ON courts(status);
CREATE INDEX idx_time_slots_court_date ON time_slots(court_id, date);
CREATE INDEX idx_time_slots_date ON time_slots(date);
CREATE INDEX idx_time_slots_status ON time_slots(status);
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_court_id ON bookings(court_id);
CREATE INDEX idx_bookings_date ON bookings(date);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_user_status ON bookings(user_id, status);
CREATE INDEX idx_payments_booking_id ON payments(booking_id);
