-- 1. Each range includes its start date and excludes its end date.
CREATE TABLE IF NOT EXISTS kskdb.salesheader_jan
    PARTITION OF kskdb.salesheader_partition_demo
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');

CREATE TABLE IF NOT EXISTS kskdb.salesheader_feb
    PARTITION OF kskdb.salesheader_partition_demo
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');

-- 2. Insert into the parent. PostgreSQL routes each sale automatically.
INSERT INTO kskdb.salesheader_partition_demo
    (transid, transdate, location, total)
SELECT seed.transid, seed.transdate::timestamp, seed.location, seed.total
FROM (VALUES
    ('1', '2026-01-10', '101', 20.00),
    ('2', '2026-01-31', '102', 30.00),
    ('3', '2026-02-01', '101', 40.00),
    ('4', '2026-02-15', '102', 50.00)
) AS seed(transid, transdate, location, total)
WHERE NOT EXISTS (
    SELECT 1
    FROM kskdb.salesheader_partition_demo AS existing
    WHERE existing.transid = seed.transid
      AND existing.transdate = seed.transdate::timestamp
);

SELECT *
FROM kskdb.salesheader_jan
ORDER BY transid;