-- inventory_db: stock levels and per-order reservations
CREATE TABLE stock_item (
    sku             VARCHAR(32)  PRIMARY KEY,
    warehouse_code  VARCHAR(20)  NOT NULL,
    on_hand         INTEGER      NOT NULL CHECK (on_hand >= 0),
    reserved        INTEGER      NOT NULL DEFAULT 0 CHECK (reserved >= 0),
    reorder_level   INTEGER      NOT NULL DEFAULT 0,
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_reserved_le_on_hand CHECK (reserved <= on_hand)
);

CREATE TABLE reservation (
    id           BIGSERIAL    PRIMARY KEY,
    order_ref    VARCHAR(64)  NOT NULL,
    sku          VARCHAR(32)  NOT NULL REFERENCES stock_item (sku),
    quantity     INTEGER      NOT NULL CHECK (quantity > 0),
    status       VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    released_at  TIMESTAMPTZ
);

CREATE INDEX idx_reservation_order_ref ON reservation (order_ref);
