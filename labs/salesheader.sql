CREATE SCHEMA IF NOT EXISTS kskdb;

-- Move an existing lab table and its partitions while preserving their data.
ALTER TABLE IF EXISTS public.salesheader RENAME TO salesheader_partition_demo;
ALTER TABLE IF EXISTS public.salesheader_partition_demo SET SCHEMA kskdb;
ALTER TABLE IF EXISTS public.salesheader_jan SET SCHEMA kskdb;
ALTER TABLE IF EXISTS public.salesheader_feb SET SCHEMA kskdb;

-- 1. Define one logical table. The transaction date determines the partition.
CREATE TABLE IF NOT EXISTS kskdb.salesheader_partition_demo (
    transid   varchar(50) NOT NULL,
    transdate timestamp NOT NULL,
    location  varchar(50) NOT NULL,
    total     numeric(10, 2) NOT NULL
) PARTITION BY RANGE (transdate);
