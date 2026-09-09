-- V2: Add interannual variation (YoY) for IPC (HU-06)
-- Stores the :percent_change_a_year_ago value directly from INDEC API to avoid recalculating on each dashboard query
ALTER TABLE indicator_value ADD COLUMN yoy_value NUMERIC(18,4);
COMMENT ON COLUMN indicator_value.yoy_value IS 'Variacion interanual % (INDEC :percent_change_a_year_ago), null for BCRA and for IPC without t-12';
