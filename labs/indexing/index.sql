CREATE INDEX IF NOT EXISTS idx_location_transdate
    ON salesheader (location, transdate);
