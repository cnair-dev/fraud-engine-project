-- V3__add_reason_codes_column.sql
-- Adds missing JSONB column to flagged_transactions for reason codes
ALTER TABLE flagged_transactions
    ADD COLUMN IF NOT EXISTS reason_codes JSONB;
