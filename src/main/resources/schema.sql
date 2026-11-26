CREATE SCHEMA IF NOT EXISTS kskdb;
ALTER TABLE IF EXISTS public.products SET SCHEMA kskdb;
ALTER TABLE IF EXISTS public.kiosk_sale_outbox SET SCHEMA kskdb;
ALTER TABLE IF EXISTS public.kiosk_inventory SET SCHEMA kskdb;
ALTER TABLE IF EXISTS public.kiosk_inventory_processed_events SET SCHEMA kskdb;

CREATE TABLE IF NOT EXISTS kskdb.products (
    id BIGINT PRIMARY KEY,
    name TEXT NOT NULL
);

INSERT INTO kskdb.products (id, name)
VALUES (123, 'Mechanical Keyboard')
ON CONFLICT (id) DO NOTHING;

ALTER TABLE IF EXISTS checkout.salesheader SET SCHEMA kskdb;
ALTER TABLE IF EXISTS checkout.salesitem SET SCHEMA kskdb;
ALTER TABLE IF EXISTS public.kiosk_sales SET SCHEMA kskdb;
ALTER TABLE IF EXISTS kskdb.kiosk_sales RENAME TO salesheader;

CREATE TABLE IF NOT EXISTS kskdb.salesheader (
    sale_id UUID PRIMARY KEY,
    amount NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    item_sku TEXT NOT NULL,
    receipt_type TEXT NOT NULL CHECK (receipt_type IN ('EMAIL', 'SMS'))
);

-- Keep the legacy SKU for backfill. New sales store their SKUs in salesitem.
ALTER TABLE kskdb.salesheader ALTER COLUMN item_sku DROP NOT NULL;

CREATE TABLE IF NOT EXISTS kskdb.salesitem (
    sale_id UUID NOT NULL REFERENCES kskdb.salesheader(sale_id),
    line_no INTEGER NOT NULL CHECK (line_no > 0),
    item_sku TEXT NOT NULL,
    PRIMARY KEY (sale_id, line_no)
);

ALTER TABLE kskdb.salesitem
    ADD COLUMN IF NOT EXISTS qty INTEGER NOT NULL DEFAULT 1 CHECK (qty > 0);

INSERT INTO kskdb.salesitem (sale_id, line_no, item_sku)
SELECT sale_id, 1, item_sku FROM kskdb.salesheader WHERE item_sku IS NOT NULL
ON CONFLICT (sale_id, line_no) DO NOTHING;

CREATE TABLE IF NOT EXISTS kskdb.kiosk_sale_outbox (
    event_id UUID PRIMARY KEY,
    sale_id UUID NOT NULL REFERENCES kskdb.salesheader(sale_id),
    payload JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS kskdb.kiosk_inventory (
    item_sku TEXT PRIMARY KEY,
    quantity INTEGER NOT NULL CHECK (quantity >= 0)
);

CREATE TABLE IF NOT EXISTS kskdb.kiosk_inventory_processed_events (
    event_id UUID PRIMARY KEY
);

INSERT INTO kskdb.kiosk_inventory (item_sku, quantity)
VALUES ('SANDWICH', 10)
ON CONFLICT (item_sku) DO NOTHING;