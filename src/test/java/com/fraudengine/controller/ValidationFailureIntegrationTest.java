package com.fraudengine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudengine.model.dto.TransactionEvaluateRequest;
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
class ValidationFailureIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    @Test
    void shouldReturn400ForInvalidRequest() throws Exception {
        var req = new TransactionEvaluateRequest();
        // Missing customerId (violates @NotBlank)
        req.setTxnId(UUID.randomUUID().toString());
        req.setAmount(BigDecimal.valueOf(100));
        req.setCurrency("USD");
        req.setTimestamp(Instant.now().toString());

        mockMvc.perform(post("/api/v1/transactions/evaluate")
                        .header("Authorization", basicAuth("admin","adminpass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    private static String basicAuth(String u, String p) {
        return "Basic " + java.util.Base64.getEncoder().encodeToString((u+":"+p).getBytes());
    }
}
