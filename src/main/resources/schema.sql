CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY,
    name TEXT NOT NULL
);

INSERT INTO products (id, name)
VALUES (123, 'Mechanical Keyboard')
ON CONFLICT (id) DO NOTHING;

CREATE TABLE IF NOT EXISTS kiosk_sales (
    sale_id UUID PRIMARY KEY,
    amount NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    item_sku TEXT NOT NULL,
    receipt_type TEXT NOT NULL CHECK (receipt_type IN ('EMAIL', 'SMS'))
);

CREATE TABLE IF NOT EXISTS kiosk_sale_outbox (
    event_id UUID PRIMARY KEY,
    sale_id UUID NOT NULL REFERENCES kiosk_sales(sale_id),
    payload JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS kiosk_inventory (
    item_sku TEXT PRIMARY KEY,
    quantity INTEGER NOT NULL CHECK (quantity >= 0)
);

CREATE TABLE IF NOT EXISTS kiosk_inventory_processed_events (
    event_id UUID PRIMARY KEY
);

INSERT INTO kiosk_inventory (item_sku, quantity)
VALUES ('SANDWICH', 10)
ON CONFLICT (item_sku) DO NOTHING;