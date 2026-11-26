-- 1. Define one logical table. The transaction date determines the partition.
CREATE TABLE salesheader (
    transid   varchar(50) NOT NULL,
    transdate timestamp NOT NULL,
    location  varchar(50) NOT NULL,
    total     numeric(10, 2) NOT NULL
) PARTITION BY RANGE (transdate);
