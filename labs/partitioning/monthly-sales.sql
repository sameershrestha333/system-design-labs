-- 1. Define one logical table. The transaction date determines the partition.
CREATE TEMP TABLE kiosk_transactions (
    transaction_id   integer NOT NULL,
    transaction_date date NOT NULL,
    location_id      integer NOT NULL,
    amount           numeric(10, 2) NOT NULL
) PARTITION BY RANGE (transaction_date);

-- 2. Each range includes its start date and excludes its end date.
CREATE TEMP TABLE kiosk_transactions_jan
    PARTITION OF kiosk_transactions
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');

CREATE TEMP TABLE kiosk_transactions_feb
    PARTITION OF kiosk_transactions
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');

-- 3. Insert into the parent. PostgreSQL routes each sale automatically.
INSERT INTO kiosk_transactions
    (transaction_id, transaction_date, location_id, amount)
VALUES
    (1, '2026-01-10', 101, 20.00),
    (2, '2026-01-31', 102, 30.00),
    (3, '2026-02-01', 101, 40.00),
    (4, '2026-02-15', 102, 50.00);

SELECT *
FROM kiosk_transactions_jan
ORDER BY transaction_id;