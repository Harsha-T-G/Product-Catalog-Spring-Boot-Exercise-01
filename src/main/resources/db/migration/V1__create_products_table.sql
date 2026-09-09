CREATE TABLE products (
    id UUID PRIMARY KEY,
    sku VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    price NUMERIC(19, 2) NOT NULL,
    stock_quantity INTEGER NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT products_price_positive CHECK (price > 0),
    CONSTRAINT products_stock_quantity_non_negative CHECK (stock_quantity >= 0)
);

CREATE UNIQUE INDEX products_sku_unique_lower ON products (LOWER(sku));
