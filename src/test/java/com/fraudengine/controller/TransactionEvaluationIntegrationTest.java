package com.fraudengine.controller;

import com.fraudengine.model.dto.TransactionEvaluateRequest;
import com.fraudengine.model.entity.Customer;
import com.fraudengine.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionEvaluationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CustomerRepository customerRepository;

    private UUID existingCustomerId;

    @BeforeEach
    void setUp() {
        Customer c = Customer.builder()
                .accountType("STANDARD")
                .riskSegment("LOW")
                .createdAt(OffsetDateTime.now())
                .historicalChargebacks(0)
                .build();
        existingCustomerId = customerRepository.save(c).getId();
    }

    @Test
    void shouldEvaluateTransactionAndReturnFlaggedResponse() throws Exception {
        TransactionEvaluateRequest req = new TransactionEvaluateRequest();
        req.setTxnId(UUID.randomUUID().toString());
        req.setCustomerId(existingCustomerId.toString()); // Use existing customer
        req.setAmount(BigDecimal.valueOf(15000));
        req.setCurrency("USD");
        req.setMerchantId("m-123");
        req.setMcc("7995");
        req.setDeviceId("d-xyz");
        req.setTimestamp(Instant.now().toString());

        mockMvc.perform(post("/api/v1/transactions/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " +
                                java.util.Base64.getEncoder().encodeToString("admin:adminpass".getBytes()))
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compositeScore").exists())
                .andExpect(jsonPath("$.decision").exists())
                .andExpect(jsonPath("$.reasonCodes").isArray());
    }
}
