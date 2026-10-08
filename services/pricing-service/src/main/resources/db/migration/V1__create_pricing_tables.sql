-- pricing_db: list prices and volume discount tiers (tiers only applied by engine v2)
CREATE TABLE price_list_entry (
    sku         VARCHAR(32)    PRIMARY KEY,
    list_price  NUMERIC(10,2)  NOT NULL CHECK (list_price >= 0),
    currency    VARCHAR(3)     NOT NULL DEFAULT 'USD',
    updated_at  TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE TABLE volume_discount_tier (
    id                BIGSERIAL     PRIMARY KEY,
    min_quantity      INTEGER       NOT NULL UNIQUE CHECK (min_quantity > 0),
    discount_percent  NUMERIC(5,2)  NOT NULL CHECK (discount_percent BETWEEN 0 AND 100)
);
