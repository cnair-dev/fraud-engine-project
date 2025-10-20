package com.fraudengine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudengine.model.dto.TransactionBatchEvaluateRequest;
import com.fraudengine.model.dto.TransactionEvaluateRequest;
import com.fraudengine.model.entity.Customer;
import com.fraudengine.repository.CustomerRepository;
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
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class TransactionBatchEvaluationIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @Autowired CustomerRepository customerRepository;

    UUID customerId;

    @BeforeEach
    void setup() {
        var c = customerRepository.save(Customer.builder()
                .accountType("STANDARD")
                .riskSegment("LOW")
                .build());
        customerId = c.getId();
    }

    @Test
    void shouldEvaluateBatch() throws Exception {
        var t1 = new TransactionEvaluateRequest();
        t1.setTxnId(UUID.randomUUID().toString());
        t1.setCustomerId(customerId.toString());
        t1.setAmount(BigDecimal.valueOf(5000));
        t1.setCurrency("USD");
        t1.setTimestamp(Instant.now().toString());
        t1.setMerchantId("m-1");
        t1.setMcc("7995");
        t1.setDeviceId("dev-1");

        var t2 = new TransactionEvaluateRequest();
        t2.setTxnId(UUID.randomUUID().toString());
        t2.setCustomerId(customerId.toString());
        t2.setAmount(BigDecimal.valueOf(50));
        t2.setCurrency("USD");
        t2.setTimestamp(Instant.now().toString());
        t2.setMerchantId("m-2");
        t2.setMcc("5411");
        t2.setDeviceId("dev-2");

        var body = new TransactionBatchEvaluateRequest();
        body.setTransactions(List.of(t1, t2));

        mockMvc.perform(post("/api/v1/transactions/batch-evaluate")
                        .header("Authorization", basicAuth("admin","adminpass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results.length()").value(2))
                .andExpect(jsonPath("$.results[0].compositeScore").exists())
                .andExpect(jsonPath("$.results[1].compositeScore").exists());
    }

    private static String basicAuth(String u, String p) {
        return "Basic " + java.util.Base64.getEncoder().encodeToString((u+":"+p).getBytes());
    }
}
