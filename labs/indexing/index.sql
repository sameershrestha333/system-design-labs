CREATE INDEX IF NOT EXISTS idx_location_transdate
    ON kskdb.salesheader_partition_demo (location, transdate);
