-- Extensión requerida para el constraint de exclusion anti solapamiento
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- ============================================================
--  app_user
-- ============================================================
CREATE TABLE app_user
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    full_name  VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'USER')),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ============================================================
--  space
-- ============================================================
CREATE TABLE space
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(255)   NOT NULL,
    type        VARCHAR(30)    NOT NULL CHECK (type IN ('OPEN_DESK', 'MEETING_ROOM', 'PRIVATE_OFFICE')),
    capacity    INTEGER        NOT NULL CHECK (capacity BETWEEN 1 AND 50),
    location    VARCHAR(255),
    floor       VARCHAR(50),
    hourly_rate NUMERIC(8, 2) NOT NULL CHECK (hourly_rate >= 0),
    status      VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

-- ============================================================
--  reservation
-- ============================================================
CREATE TABLE reservation
(
    id BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id           BIGINT      NOT NULL REFERENCES app_user (id),
    space_id          BIGINT      NOT NULL REFERENCES space (id),
    start_datetime    TIMESTAMPTZ NOT NULL,
    end_datetime      TIMESTAMPTZ NOT NULL,
    status            VARCHAR(30) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'PENDING_PAYMENT', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    payment_reference VARCHAR(255),
    total_price       NUMERIC(10, 2),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    version           BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT chk_reservation_time CHECK (end_datetime > start_datetime)
);

-- indice para acelerar las búsquedas de reservas por espacio
CREATE INDEX idx_reservation_space ON reservation (space_id);
-- indice para las consultas "mis reservas" del usuario
CREATE INDEX idx_reservation_user ON reservation (user_id);

-- ============================================================
-- creando exclusion para que dos reservas del mismo espacio no puedan
-- solaparse en el tiempo
-- ============================================================
ALTER TABLE reservation
    ADD CONSTRAINT no_overlapping_reservations EXCLUDE USING gist (
space_id WITH =,
tstzrange(start_datetime, end_datetime) WITH &&
) WHERE (status IN ('PENDING', 'PENDING_PAYMENT', 'CONFIRMED'));