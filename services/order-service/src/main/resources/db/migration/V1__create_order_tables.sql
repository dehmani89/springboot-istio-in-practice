-- order_db: orders and order lines (product name + price snapshot at order time)
CREATE TABLE customer_order (
    id                      BIGSERIAL      PRIMARY KEY,
    order_number            VARCHAR(64)    NOT NULL UNIQUE,
    customer_id             VARCHAR(40)    NOT NULL,
    status                  VARCHAR(20)    NOT NULL,
    currency                VARCHAR(3)     NOT NULL,
    total_amount            NUMERIC(12,2)  NOT NULL,
    pricing_engine_version  VARCHAR(10)    NOT NULL,
    created_at              TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_customer_order_customer ON customer_order (customer_id, created_at DESC);

CREATE TABLE order_line (
    id                BIGSERIAL      PRIMARY KEY,
    order_id          BIGINT         NOT NULL REFERENCES customer_order (id) ON DELETE CASCADE,
    sku               VARCHAR(32)    NOT NULL,
    product_name      VARCHAR(200)   NOT NULL,
    quantity          INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price        NUMERIC(10,2)  NOT NULL,
    discount_percent  NUMERIC(5,2)   NOT NULL DEFAULT 0,
    line_total        NUMERIC(12,2)  NOT NULL
);

CREATE INDEX idx_order_line_order ON order_line (order_id);
