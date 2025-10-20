package com.fraudengine.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudengine.model.entity.Customer;
import com.fraudengine.model.entity.FlaggedTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FlaggedTransactionRepositoryTest {

    @Autowired
    private FlaggedTransactionRepository flaggedTransactionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private final ObjectMapper mapper = new ObjectMapper();
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = customerRepository.save(Customer.builder()
                .id(UUID.randomUUID())
                .accountType("STANDARD")
                .riskSegment("NORMAL")
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build());
    }

    @Test
    void shouldPersistAndRetrieveFlaggedTransactionWithJsonFields() {
        JsonNode reasons = mapper.valueToTree(List.of("AMOUNT_SPIKE", "VELOCITY"));
        JsonNode details = mapper.valueToTree(Map.of(
                "rule", "HighAmount",
                "threshold", 5000,
                "actual", 9500
        ));

        FlaggedTransaction txn = flaggedTransactionRepository.save(
                FlaggedTransaction.builder()
                        .txnId(UUID.randomUUID())
                        .customerId(customer.getId())
                        .score(85)
                        .decision("DECLINE")
                        .reasonCodes(reasons)
                        .details(details)
                        .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .build()
        );

        FlaggedTransaction retrieved = flaggedTransactionRepository.findById(txn.getId()).orElseThrow();

        assertThat(retrieved.getDecision()).isEqualTo("DECLINE");
        assertThat(retrieved.getReasonCodes().isArray()).isTrue();
        assertThat(retrieved.getReasonCodes().toString()).contains("AMOUNT_SPIKE");
        assertThat(retrieved.getDetails().get("rule").asText()).isEqualTo("HighAmount");
    }
}
