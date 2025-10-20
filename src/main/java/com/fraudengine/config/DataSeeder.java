package com.fraudengine.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void seedData() {
        try {
            var resource = new ClassPathResource("db/seed-data.sql");
            if (!resource.exists()) {
                log.warn("No seed-data.sql found — skipping data seeding.");
                return;
            }

            log.info("Seeding demo data from classpath: db/seed-data.sql ...");

            try (InputStream in = resource.getInputStream()) {
                String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                jdbcTemplate.execute(sql);
            }

            // Verify and log counts
            Integer customers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class);
            Integer txns = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transactions", Integer.class);
            Integer flagged = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM flagged_transactions", Integer.class);

            log.info("Seed completed. Customers={}, Transactions={}, Flagged={}", customers, txns, flagged);

        } catch (Exception e) {
            log.error("Failed to execute seed-data.sql", e);
        }
    }
}
