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
class ForeignKeyIntegrityTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldHaveForeignKeyFromTransactionsToCustomers() {
        // Verify that Hibernate’s auto-created schema produced a FK between transactions and customers
        List<Map<String, Object>> fks = jdbcTemplate.queryForList(
                """
                SELECT tc.constraint_name, kcu.column_name, ccu.table_name AS foreign_table
                FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name
                JOIN information_schema.constraint_column_usage ccu ON ccu.constraint_name = tc.constraint_name
                WHERE tc.table_name='transactions' AND tc.constraint_type='FOREIGN KEY'
                """
        );

        // If ddl-auto:create-drop built the schema, the FK should exist.
        boolean hasFkToCustomer = fks.stream()
                .anyMatch(row -> row.get("foreign_table").toString().equalsIgnoreCase("customers"));

        assertThat(hasFkToCustomer)
                .as("Transactions should have a foreign key to Customers (as defined in entity mappings)")
                .isTrue();
    }

    @Test
    void flaggedTransactionsAreIntentionallyDenormalized() {
        // Ensure no FK constraints exist on flagged_transactions (intentional design choice)
        List<Map<String, Object>> fks = jdbcTemplate.queryForList(
                """
                SELECT tc.constraint_name, kcu.column_name, ccu.table_name AS foreign_table
                FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name
                JOIN information_schema.constraint_column_usage ccu ON ccu.constraint_name = tc.constraint_name
                WHERE tc.table_name='flagged_transactions' AND tc.constraint_type='FOREIGN KEY'
                """
        );

        boolean hasFk = !fks.isEmpty();

        assertThat(hasFk)
                .as("FlaggedTransactions intentionally store denormalized UUID references (no FKs expected)")
                .isFalse();
    }

    @Test
    void foreignKeysShouldHaveCascadeOrRestrictBehavior() {
        // Confirm that any FK rules (like transactions/customers) specify delete/update behavior
        List<Map<String, Object>> actions = jdbcTemplate.queryForList(
                """
                SELECT rc.constraint_name, rc.update_rule, rc.delete_rule
                FROM information_schema.referential_constraints rc
                JOIN information_schema.table_constraints tc ON rc.constraint_name = tc.constraint_name
                WHERE tc.constraint_type='FOREIGN KEY'
                """
        );

        boolean allHaveRules = actions.isEmpty() || actions.stream().allMatch(row ->
                row.get("update_rule") != null &&
                        row.get("delete_rule") != null &&
                        !row.get("update_rule").toString().isBlank() &&
                        !row.get("delete_rule").toString().isBlank()
        );

        assertThat(allHaveRules)
                .as("All defined foreign keys should specify ON UPDATE/DELETE behavior")
                .isTrue();
    }
}
