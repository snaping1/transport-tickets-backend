-- V11: Таблицы пассажиров и платежей
-- passengers → tickets, passenger → ticket_seats (через seat_id)
-- payments   → tickets, payments  → users

CREATE TABLE passengers (
    id              SERIAL PRIMARY KEY,
    ticket_id       INT NOT NULL REFERENCES tickets(id)      ON DELETE CASCADE,
    seat_id         INT          REFERENCES ticket_seats(id) ON DELETE SET NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    patronymic      VARCHAR(100),
    document_type   VARCHAR(30)  NOT NULL DEFAULT 'passport',
    document_series VARCHAR(10),
    document_number VARCHAR(20)  NOT NULL,
    birth_date      DATE,
    gender          VARCHAR(10)  NOT NULL DEFAULT 'male',
    CONSTRAINT chk_passenger_gender   CHECK (gender IN ('male', 'female')),
    CONSTRAINT chk_passenger_doc_type CHECK (document_type IN ('passport', 'foreign_passport', 'birth_cert'))
);

CREATE TABLE payments (
    id              SERIAL PRIMARY KEY,
    ticket_id       INT NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    user_id         INT NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    amount          NUMERIC(10,2) NOT NULL,
    payment_method  VARCHAR(50)  NOT NULL DEFAULT 'card',
    status          VARCHAR(20)  NOT NULL DEFAULT 'completed',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_payment_status  CHECK (status IN ('pending', 'completed', 'refunded')),
    CONSTRAINT chk_payment_amount  CHECK (amount > 0),
    CONSTRAINT payments_ticket_unique UNIQUE (ticket_id)
);

CREATE INDEX idx_passengers_ticket ON passengers(ticket_id);
CREATE INDEX idx_passengers_seat   ON passengers(seat_id);
CREATE INDEX idx_payments_ticket   ON payments(ticket_id);
CREATE INDEX idx_payments_user     ON payments(user_id);
CREATE INDEX idx_payments_status   ON payments(status);
