package com.fraudengine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class TransactionEvaluationControllerSmokeTest {

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
    void controllerShouldHandleEvaluationRequest() throws Exception {
        var req = new TransactionEvaluateRequest();
        req.setTxnId(UUID.randomUUID().toString());
        req.setCustomerId(customerId.toString());
        req.setAmount(BigDecimal.valueOf(3000));
        req.setCurrency("USD");
        req.setTimestamp(Instant.now().toString());
        req.setMerchantId("m-1");
        req.setMcc("7995");
        req.setDeviceId("d-1");

        mockMvc.perform(post("/api/v1/transactions/evaluate")
                        .header("Authorization", basicAuth("admin","adminpass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compositeScore").exists());
    }

    private static String basicAuth(String u, String p) {
        return "Basic " + java.util.Base64.getEncoder().encodeToString((u+":"+p).getBytes());
    }
}
