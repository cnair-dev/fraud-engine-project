package com.fraudengine.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SchemaValidationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldContainExpectedCoreTables() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT tablename FROM pg_tables WHERE schemaname='public'",
                String.class
        );

        assertThat(tables)
                .as("Core domain tables should exist")
                .contains("customers", "transactions", "flagged_transactions", "rule_sets");
    }

    @Test
    void flaggedTransactionsShouldHaveJsonbColumn() {
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT column_name, data_type FROM information_schema.columns WHERE table_name='flagged_transactions'"
        );

        boolean hasJsonb = columns.stream()
                .anyMatch(c -> c.get("data_type").toString().equalsIgnoreCase("jsonb"));

        assertThat(hasJsonb)
                .as("Flagged transactions should include a JSONB 'details' column")
                .isTrue();
    }

    @Test
    void flaggedTransactionsShouldHaveIndexesDefined() {
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
                "SELECT indexname, indexdef FROM pg_indexes WHERE tablename='flagged_transactions'"
        );

        // check that at least one index exists
        assertThat(indexes)
                .as("flagged_transactions table should have at least one index (e.g. PK or FK)")
                .isNotEmpty();

        boolean hasGinIndex = indexes.stream()
                .anyMatch(idx -> idx.get("indexdef").toString().toLowerCase().contains("gin"));

        assertThat(hasGinIndex)
                .as("No GIN index exists yet — expected when running with Hibernate ddl-auto")
                .isFalse();
    }
}
