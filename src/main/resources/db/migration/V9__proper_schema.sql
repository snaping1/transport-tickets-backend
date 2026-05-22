-- ═══════════════════════════════════════════════════════════════════════════════
-- V9: Нормализация схемы
--   • Таблица cities — справочник городов, FK в routes
--   • ticket_seats   — замена текстового столбца seat_numbers (1НФ)
--   • CHECK-ограничения на routes и tickets
--   • Очистка users (firebase_uid, UNIQUE email, NOT NULL password_hash)
--   • Пересборка индексов
-- ═══════════════════════════════════════════════════════════════════════════════

-- ── 1. Справочник городов ─────────────────────────────────────────────────────
CREATE TABLE cities (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT cities_name_unique UNIQUE (name)
);

INSERT INTO cities (name)
SELECT DISTINCT unnest(ARRAY[origin_city, destination_city])
FROM routes
ON CONFLICT (name) DO NOTHING;

-- ── 2. FK-столбцы в routes ────────────────────────────────────────────────────
ALTER TABLE routes
    ADD COLUMN origin_city_id      INT,
    ADD COLUMN destination_city_id INT;

UPDATE routes
SET origin_city_id      = (SELECT id FROM cities WHERE name = routes.origin_city),
    destination_city_id = (SELECT id FROM cities WHERE name = routes.destination_city);

ALTER TABLE routes
    ALTER COLUMN origin_city_id      SET NOT NULL,
    ALTER COLUMN destination_city_id SET NOT NULL,
    ADD CONSTRAINT fk_routes_origin      FOREIGN KEY (origin_city_id)      REFERENCES cities(id),
    ADD CONSTRAINT fk_routes_destination FOREIGN KEY (destination_city_id) REFERENCES cities(id);

-- Удалить старые текстовые столбцы и устаревшие индексы на них
DROP INDEX IF EXISTS idx_routes_origin;
DROP INDEX IF EXISTS idx_routes_destination;

ALTER TABLE routes
    DROP COLUMN origin_city,
    DROP COLUMN destination_city;

-- ── 3. CHECK-ограничения на routes ───────────────────────────────────────────
-- Сначала корректируем возможные «выбившиеся» данные
UPDATE routes SET available_seats = GREATEST(0, LEAST(available_seats, total_seats));

ALTER TABLE routes
    ADD CONSTRAINT chk_routes_price     CHECK (price > 0),
    ADD CONSTRAINT chk_routes_seats     CHECK (total_seats > 0
                                             AND available_seats >= 0
                                             AND available_seats <= total_seats),
    ADD CONSTRAINT chk_routes_times     CHECK (departure_time < arrival_time),
    ADD CONSTRAINT chk_routes_transport CHECK (transport_type IN ('bus', 'train', 'plane'));

-- ── 4. Таблица мест билета (вместо TEXT-поля seat_numbers) ───────────────────
CREATE TABLE ticket_seats (
    id          SERIAL PRIMARY KEY,
    ticket_id   INT NOT NULL REFERENCES tickets(id)  ON DELETE CASCADE,
    route_id    INT NOT NULL REFERENCES routes(id),
    seat_number INT NOT NULL CHECK (seat_number > 0),
    CONSTRAINT ticket_seats_unique UNIQUE (ticket_id, seat_number)
);

-- Миграция существующих данных из текстового столбца
INSERT INTO ticket_seats (ticket_id, route_id, seat_number)
SELECT t.id,
       t.route_id,
       elem.s::int
FROM tickets t
CROSS JOIN LATERAL unnest(
    string_to_array(NULLIF(TRIM(t.seat_numbers), ''), ',')
) AS elem(s)
WHERE t.seat_numbers IS NOT NULL
  AND TRIM(t.seat_numbers) != '';

ALTER TABLE tickets DROP COLUMN IF EXISTS seat_numbers;

ALTER TABLE tickets
    ADD CONSTRAINT chk_tickets_seat_count CHECK (seat_count > 0),
    ADD CONSTRAINT chk_tickets_price      CHECK (total_price > 0);

-- ── 5. Очистка таблицы users ─────────────────────────────────────────────────
-- Удалить пользователей без пароля (Firebase-записи, оставшиеся от старой схемы)
DELETE FROM tickets WHERE user_id IN (SELECT id FROM users WHERE password_hash IS NULL);
DELETE FROM users WHERE password_hash IS NULL;

ALTER TABLE users
    DROP COLUMN  IF EXISTS firebase_uid,
    ALTER COLUMN password_hash SET NOT NULL,
    ADD CONSTRAINT users_email_unique UNIQUE (email),
    ADD CONSTRAINT chk_users_email CHECK (char_length(email) >= 3 AND position('@' IN email) > 0);

-- ── 6. Пересборка индексов ────────────────────────────────────────────────────
DROP INDEX IF EXISTS idx_routes_departure;
DROP INDEX IF EXISTS idx_tickets_user;

-- Cities
CREATE INDEX idx_cities_name             ON cities(LOWER(name));

-- Routes
CREATE INDEX idx_routes_origin           ON routes(origin_city_id);
CREATE INDEX idx_routes_destination      ON routes(destination_city_id);
CREATE INDEX idx_routes_departure        ON routes(departure_time);
-- Составной индекс для типичного поискового запроса
CREATE INDEX idx_routes_search           ON routes(origin_city_id, destination_city_id, departure_time);

-- Tickets
CREATE INDEX idx_tickets_user            ON tickets(user_id);
CREATE INDEX idx_tickets_route           ON tickets(route_id);
CREATE INDEX idx_tickets_status          ON tickets(status);
-- Ускоряет выборку «активных билетов пользователя»
CREATE INDEX idx_tickets_user_status     ON tickets(user_id, status);

-- Ticket seats
CREATE INDEX idx_ticket_seats_ticket     ON ticket_seats(ticket_id);
-- Ключевой индекс: проверка занятости мест по маршруту
CREATE INDEX idx_ticket_seats_route_seat ON ticket_seats(route_id, seat_number);

-- Users
CREATE INDEX idx_users_email             ON users(email);
