-- Adds JSONB transaction snapshot for audit/review on flags
ALTER TABLE flagged_transactions
    ADD COLUMN IF NOT EXISTS txn_snapshot JSONB;
