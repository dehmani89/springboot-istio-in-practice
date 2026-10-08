-- catalog_db: dental product master data
CREATE TABLE product (
    id               BIGSERIAL     PRIMARY KEY,
    sku              VARCHAR(32)   NOT NULL UNIQUE,
    name             VARCHAR(200)  NOT NULL,
    category         VARCHAR(50)   NOT NULL,
    manufacturer     VARCHAR(100)  NOT NULL,
    description      TEXT,
    unit_of_measure  VARCHAR(30)   NOT NULL,
    active           BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_product_category ON product (category);
