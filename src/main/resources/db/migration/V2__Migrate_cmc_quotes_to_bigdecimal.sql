-- ============================================================================
-- Migration: V2 - Migrate cmc_quotes_latest table String columns to DECIMAL
-- Purpose: Convert numeric fields from String to BigDecimal
-- Date: 2025-05-03
-- ============================================================================

-- Step 1: Add new DECIMAL columns for supply-related fields
ALTER TABLE cmc_quotes_latest ADD COLUMN IF NOT EXISTS max_supply_num DECIMAL(30, 8);
ALTER TABLE cmc_quotes_latest ADD COLUMN IF NOT EXISTS circulating_supply_num DECIMAL(30, 8);
ALTER TABLE cmc_quotes_latest ADD COLUMN IF NOT EXISTS total_supply_num DECIMAL(30, 8);

-- Step 2: Add new DECIMAL columns for quote-related fields
ALTER TABLE cmc_quotes_latest ADD COLUMN IF NOT EXISTS price_num DECIMAL(25, 12);
ALTER TABLE cmc_quotes_latest ADD COLUMN IF NOT EXISTS volume_24h_num DECIMAL(30, 2);
ALTER TABLE cmc_quotes_latest ADD COLUMN IF NOT EXISTS volume_change_24h_num DECIMAL(20, 8);
ALTER TABLE cmc_quotes_latest ADD COLUMN IF NOT EXISTS percent_change_1h_num DECIMAL(20, 8);
ALTER TABLE cmc_quotes_latest ADD COLUMN IF NOT EXISTS percent_change_24h_num DECIMAL(20, 8);
ALTER TABLE cmc_quotes_latest ADD COLUMN IF NOT EXISTS percent_change_7d_num DECIMAL(20, 8);
ALTER TABLE cmc_quotes_latest ADD COLUMN IF NOT EXISTS percent_change_30d_num DECIMAL(20, 8);
ALTER TABLE cmc_quotes_latest ADD COLUMN IF NOT EXISTS percent_change_60d_num DECIMAL(20, 8);
ALTER TABLE cmc_quotes_latest ADD COLUMN IF NOT EXISTS percent_change_90d_num DECIMAL(20, 8);
ALTER TABLE cmc_quotes_latest ADD COLUMN IF NOT EXISTS market_cap_num DECIMAL(30, 2);
ALTER TABLE cmc_quotes_latest ADD COLUMN IF NOT EXISTS market_cap_dominance_num DECIMAL(10, 8);

-- Step 3: Migrate data from String columns to DECIMAL columns
UPDATE cmc_quotes_latest SET
    max_supply_num = CASE WHEN max_supply IS NULL OR TRIM(max_supply) = '' THEN NULL ELSE CAST(max_supply AS DECIMAL(30, 8)) END,
    circulating_supply_num = CASE WHEN circulating_supply IS NULL OR TRIM(circulating_supply) = '' THEN NULL ELSE CAST(circulating_supply AS DECIMAL(30, 8)) END,
    total_supply_num = CASE WHEN total_supply IS NULL OR TRIM(total_supply) = '' THEN NULL ELSE CAST(total_supply AS DECIMAL(30, 8)) END,
    price_num = CASE WHEN price IS NULL OR TRIM(price) = '' THEN NULL ELSE CAST(price AS DECIMAL(25, 12)) END,
    volume_24h_num = CASE WHEN volume_24h IS NULL OR TRIM(volume_24h) = '' THEN NULL ELSE CAST(volume_24h AS DECIMAL(30, 2)) END,
    volume_change_24h_num = CASE WHEN volume_change_24h IS NULL OR TRIM(volume_change_24h) = '' THEN NULL ELSE CAST(volume_change_24h AS DECIMAL(20, 8)) END,
    percent_change_1h_num = CASE WHEN percent_change_1h IS NULL OR TRIM(percent_change_1h) = '' THEN NULL ELSE CAST(percent_change_1h AS DECIMAL(20, 8)) END,
    percent_change_24h_num = CASE WHEN percent_change_24h IS NULL OR TRIM(percent_change_24h) = '' THEN NULL ELSE CAST(percent_change_24h AS DECIMAL(20, 8)) END,
    percent_change_7d_num = CASE WHEN percent_change_7d IS NULL OR TRIM(percent_change_7d) = '' THEN NULL ELSE CAST(percent_change_7d AS DECIMAL(20, 8)) END,
    percent_change_30d_num = CASE WHEN percent_change_30d IS NULL OR TRIM(percent_change_30d) = '' THEN NULL ELSE CAST(percent_change_30d AS DECIMAL(20, 8)) END,
    percent_change_60d_num = CASE WHEN percent_change_60d IS NULL OR TRIM(percent_change_60d) = '' THEN NULL ELSE CAST(percent_change_60d AS DECIMAL(20, 8)) END,
    percent_change_90d_num = CASE WHEN percent_change_90d IS NULL OR TRIM(percent_change_90d) = '' THEN NULL ELSE CAST(percent_change_90d AS DECIMAL(20, 8)) END,
    market_cap_num = CASE WHEN market_cap IS NULL OR TRIM(market_cap) = '' THEN NULL ELSE CAST(market_cap AS DECIMAL(30, 2)) END,
    market_cap_dominance_num = CASE WHEN market_cap_dominance IS NULL OR TRIM(market_cap_dominance) = '' THEN NULL ELSE CAST(market_cap_dominance AS DECIMAL(10, 8)) END;

-- Step 4: Verify data integrity - check for any failed conversions
SELECT 'WARNING: Failed max_supply conversions' as message, COUNT(*) as count
FROM cmc_quotes_latest
WHERE max_supply IS NOT NULL AND TRIM(max_supply) != '' AND max_supply_num IS NULL
UNION ALL
SELECT 'WARNING: Failed circulating_supply conversions', COUNT(*)
FROM cmc_quotes_latest
WHERE circulating_supply IS NOT NULL AND TRIM(circulating_supply) != '' AND circulating_supply_num IS NULL
UNION ALL
SELECT 'WARNING: Failed price conversions', COUNT(*)
FROM cmc_quotes_latest
WHERE price IS NOT NULL AND TRIM(price) != '' AND price_num IS NULL
UNION ALL
SELECT 'WARNING: Failed volume_24h conversions', COUNT(*)
FROM cmc_quotes_latest
WHERE volume_24h IS NOT NULL AND TRIM(volume_24h) != '' AND volume_24h_num IS NULL;

-- Step 5: Drop old String columns
ALTER TABLE cmc_quotes_latest DROP COLUMN IF EXISTS max_supply;
ALTER TABLE cmc_quotes_latest DROP COLUMN IF EXISTS circulating_supply;
ALTER TABLE cmc_quotes_latest DROP COLUMN IF EXISTS total_supply;
ALTER TABLE cmc_quotes_latest DROP COLUMN IF EXISTS price;
ALTER TABLE cmc_quotes_latest DROP COLUMN IF EXISTS volume_24h;
ALTER TABLE cmc_quotes_latest DROP COLUMN IF EXISTS volume_change_24h;
ALTER TABLE cmc_quotes_latest DROP COLUMN IF EXISTS percent_change_1h;
ALTER TABLE cmc_quotes_latest DROP COLUMN IF EXISTS percent_change_24h;
ALTER TABLE cmc_quotes_latest DROP COLUMN IF EXISTS percent_change_7d;
ALTER TABLE cmc_quotes_latest DROP COLUMN IF EXISTS percent_change_30d;
ALTER TABLE cmc_quotes_latest DROP COLUMN IF EXISTS percent_change_60d;
ALTER TABLE cmc_quotes_latest DROP COLUMN IF EXISTS percent_change_90d;
ALTER TABLE cmc_quotes_latest DROP COLUMN IF EXISTS market_cap;
ALTER TABLE cmc_quotes_latest DROP COLUMN IF EXISTS market_cap_dominance;

-- Step 6: Rename new columns to original names
ALTER TABLE cmc_quotes_latest ALTER COLUMN max_supply_num RENAME TO max_supply;
ALTER TABLE cmc_quotes_latest ALTER COLUMN circulating_supply_num RENAME TO circulating_supply;
ALTER TABLE cmc_quotes_latest ALTER COLUMN total_supply_num RENAME TO total_supply;
ALTER TABLE cmc_quotes_latest ALTER COLUMN price_num RENAME TO price;
ALTER TABLE cmc_quotes_latest ALTER COLUMN volume_24h_num RENAME TO volume_24h;
ALTER TABLE cmc_quotes_latest ALTER COLUMN volume_change_24h_num RENAME TO volume_change_24h;
ALTER TABLE cmc_quotes_latest ALTER COLUMN percent_change_1h_num RENAME TO percent_change_1h;
ALTER TABLE cmc_quotes_latest ALTER COLUMN percent_change_24h_num RENAME TO percent_change_24h;
ALTER TABLE cmc_quotes_latest ALTER COLUMN percent_change_7d_num RENAME TO percent_change_7d;
ALTER TABLE cmc_quotes_latest ALTER COLUMN percent_change_30d_num RENAME TO percent_change_30d;
ALTER TABLE cmc_quotes_latest ALTER COLUMN percent_change_60d_num RENAME TO percent_change_60d;
ALTER TABLE cmc_quotes_latest ALTER COLUMN percent_change_90d_num RENAME TO percent_change_90d;
ALTER TABLE cmc_quotes_latest ALTER COLUMN market_cap_num RENAME TO market_cap;
ALTER TABLE cmc_quotes_latest ALTER COLUMN market_cap_dominance_num RENAME TO market_cap_dominance;

-- ============================================================================
-- Migration complete: cmc_quotes_latest table columns converted to DECIMAL
-- ============================================================================
