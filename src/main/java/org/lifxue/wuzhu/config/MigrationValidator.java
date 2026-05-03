package org.lifxue.wuzhu.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Validates database migration integrity after Flyway migrations complete.
 * Checks for any data conversion failures or inconsistencies.
 */
@Slf4j
@Component
public class MigrationValidator {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MigrationValidator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void validateMigration() {
        log.info("Starting migration validation...");

        try {
            validateTradeInfoTable();
            validateCmcQuotesLatestTable();
            log.info("Migration validation completed successfully.");
        } catch (Exception e) {
            log.error("Migration validation failed: {}", e.getMessage(), e);
        }
    }

    private void validateTradeInfoTable() {
        log.info("Validating trade_info table...");

        String countNullSql = "SELECT COUNT(*) FROM trade_info WHERE price IS NULL OR base_num IS NULL OR quote_num IS NULL";
        Integer nullCount = jdbcTemplate.queryForObject(countNullSql, Integer.class);

        if (nullCount != null && nullCount > 0) {
            log.warn("Found {} records with NULL numeric values in trade_info table", nullCount);
        }

        String checkPrecisionSql = "SELECT id, price, base_num, quote_num FROM trade_info WHERE price < 0 OR base_num < 0 OR quote_num < 0 LIMIT 10";
        List<Map<String, Object>> negativeValues = jdbcTemplate.queryForList(checkPrecisionSql);

        if (!negativeValues.isEmpty()) {
            log.warn("Found {} records with negative values in trade_info table", negativeValues.size());
            negativeValues.forEach(row -> log.warn("Negative value found: {}", row));
        }

        log.info("trade_info table validation completed.");
    }

    private void validateCmcQuotesLatestTable() {
        log.info("Validating cmc_quotes_latest table...");

        String countNullSql = "SELECT COUNT(*) FROM cmc_quotes_latest WHERE price IS NULL";
        Integer nullCount = jdbcTemplate.queryForObject(countNullSql, Integer.class);

        if (nullCount != null && nullCount > 0) {
            log.warn("Found {} records with NULL price values in cmc_quotes_latest table", nullCount);
        }

        String checkInvalidPercentSql = "SELECT id, symbol, percent_change_24h FROM cmc_quotes_latest " +
                "WHERE percent_change_24h < -100 OR percent_change_24h > 1000 LIMIT 10";
        List<Map<String, Object>> invalidPercents = jdbcTemplate.queryForList(checkInvalidPercentSql);

        if (!invalidPercents.isEmpty()) {
            log.warn("Found {} records with suspicious percentage values in cmc_quotes_latest table", invalidPercents.size());
        }

        log.info("cmc_quotes_latest table validation completed.");
    }
}
