-- 1. Each range includes its start date and excludes its end date.
CREATE TABLE salesheader_jan
    PARTITION OF salesheader
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');

CREATE TABLE salesheader_feb
    PARTITION OF salesheader
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');

-- 2. Insert into the parent. PostgreSQL routes each sale automatically.
INSERT INTO salesheader
    (transid, transdate, location, total)
VALUES
    ('1', '2026-01-10', '101', 20.00),
    ('2', '2026-01-31', '102', 30.00),
    ('3', '2026-02-01', '101', 40.00),
    ('4', '2026-02-15', '102', 50.00);

SELECT *
FROM salesheader_jan
ORDER BY transid;