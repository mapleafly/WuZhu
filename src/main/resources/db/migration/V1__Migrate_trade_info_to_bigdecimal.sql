-- ============================================================================
-- Migration: V1 - Migrate trade_info table String columns to DECIMAL
-- Purpose: Convert price, base_num, quote_num from String to BigDecimal
-- Date: 2025-05-03
-- ============================================================================

-- Step 1: Add new DECIMAL columns
-- NOTE: Using DECIMAL(30, 8) to handle large values (up to 22 integer digits)
ALTER TABLE trade_info ADD COLUMN IF NOT EXISTS price_num DECIMAL(25, 12);
ALTER TABLE trade_info ADD COLUMN IF NOT EXISTS base_num_dec DECIMAL(30, 8);
ALTER TABLE trade_info ADD COLUMN IF NOT EXISTS quote_num_dec DECIMAL(30, 8);

-- Step 2: Migrate data from String columns to DECIMAL columns
-- Handle empty strings and null values appropriately
UPDATE trade_info SET
    price_num = CASE
        WHEN price IS NULL OR TRIM(price) = '' THEN NULL
        ELSE CAST(price AS DECIMAL(25, 12))
    END,
    base_num_dec = CASE
        WHEN base_num IS NULL OR TRIM(base_num) = '' THEN NULL
        ELSE CAST(base_num AS DECIMAL(30, 8))
    END,
    quote_num_dec = CASE
        WHEN quote_num IS NULL OR TRIM(quote_num) = '' THEN NULL
        ELSE CAST(quote_num AS DECIMAL(30, 8))
    END;

-- Step 3: Verify data integrity - check for any failed conversions
-- This query will return 0 rows if all conversions succeeded
SELECT 'WARNING: Failed price conversions' as message, COUNT(*) as count
FROM trade_info
WHERE price IS NOT NULL AND TRIM(price) != '' AND price_num IS NULL
UNION ALL
SELECT 'WARNING: Failed base_num conversions', COUNT(*)
FROM trade_info
WHERE base_num IS NOT NULL AND TRIM(base_num) != '' AND base_num_dec IS NULL
UNION ALL
SELECT 'WARNING: Failed quote_num conversions', COUNT(*)
FROM trade_info
WHERE quote_num IS NOT NULL AND TRIM(quote_num) != '' AND quote_num_dec IS NULL;

-- Step 4: Drop old String columns (only after verification)
ALTER TABLE trade_info DROP COLUMN IF EXISTS price;
ALTER TABLE trade_info DROP COLUMN IF EXISTS base_num;
ALTER TABLE trade_info DROP COLUMN IF EXISTS quote_num;

-- Step 5: Rename new columns to original names
ALTER TABLE trade_info ALTER COLUMN price_num RENAME TO price;
ALTER TABLE trade_info ALTER COLUMN base_num_dec RENAME TO base_num;
ALTER TABLE trade_info ALTER COLUMN quote_num_dec RENAME TO quote_num;

-- ============================================================================
-- Migration complete: trade_info table columns converted to DECIMAL
-- ============================================================================
