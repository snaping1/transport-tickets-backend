CREATE TABLE IF NOT EXISTS users (
    id          SERIAL PRIMARY KEY,
    firebase_uid VARCHAR(128) UNIQUE NOT NULL,
    email       VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS routes (
    id              SERIAL PRIMARY KEY,
    origin_city     VARCHAR(100) NOT NULL,
    destination_city VARCHAR(100) NOT NULL,
    departure_time  TIMESTAMPTZ NOT NULL,
    arrival_time    TIMESTAMPTZ NOT NULL,
    price           NUMERIC(10,2) NOT NULL,
    total_seats     INT NOT NULL DEFAULT 50,
    available_seats INT NOT NULL DEFAULT 50,
    transport_type  VARCHAR(50) NOT NULL DEFAULT 'bus',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS tickets (
    id          SERIAL PRIMARY KEY,
    user_id     INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    route_id    INT NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    seat_count  INT NOT NULL DEFAULT 1,
    total_price NUMERIC(10,2) NOT NULL,
    status      VARCHAR(50) NOT NULL DEFAULT 'active',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_status CHECK (status IN ('active', 'cancelled'))
);

CREATE INDEX idx_routes_origin ON routes(origin_city);
CREATE INDEX idx_routes_destination ON routes(destination_city);
CREATE INDEX idx_routes_departure ON routes(departure_time);
CREATE INDEX idx_tickets_user ON tickets(user_id);

-- Seed demo data
INSERT INTO routes (origin_city, destination_city, departure_time, arrival_time, price, total_seats, available_seats, transport_type) VALUES
('Москва', 'Санкт-Петербург', NOW() + INTERVAL '1 day', NOW() + INTERVAL '1 day 4 hours', 1500.00, 50, 50, 'bus'),
('Москва', 'Казань', NOW() + INTERVAL '2 days', NOW() + INTERVAL '2 days 8 hours', 2200.00, 40, 40, 'train'),
('Санкт-Петербург', 'Москва', NOW() + INTERVAL '1 day 6 hours', NOW() + INTERVAL '1 day 10 hours', 1500.00, 50, 48, 'bus'),
('Казань', 'Москва', NOW() + INTERVAL '3 days', NOW() + INTERVAL '3 days 8 hours', 2100.00, 40, 35, 'train'),
('Москва', 'Нижний Новгород', NOW() + INTERVAL '1 day', NOW() + INTERVAL '1 day 5 hours', 900.00, 60, 60, 'bus'),
('Москва', 'Самара', NOW() + INTERVAL '4 days', NOW() + INTERVAL '4 days 12 hours', 2800.00, 45, 45, 'train'),
('Санкт-Петербург', 'Казань', NOW() + INTERVAL '5 days', NOW() + INTERVAL '5 days 16 hours', 3200.00, 40, 40, 'bus'),
('Новосибирск', 'Москва', NOW() + INTERVAL '2 days', NOW() + INTERVAL '2 days 40 hours', 7500.00, 200, 180, 'train');
