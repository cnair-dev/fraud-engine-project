// file: src/test/java/com/fraudengine/controller/FlagsPaginationIntegrationTest.java
package com.fraudengine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class FlagsPaginationIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    @Test
    void shouldPageAndFilterFlags() throws Exception {
        String customerId = UUID.randomUUID().toString();

        mockMvc.perform(get("/api/v1/transactions/flags")
                        .header("Authorization", basicAuth("admin","adminpass"))
                        .param("customerId", customerId)
                        .param("decision", "REVIEW")
                        .param("minScore", "40")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.size").value(10));
    }

    private static String basicAuth(String u, String p) {
        return "Basic " + java.util.Base64.getEncoder().encodeToString((u+":"+p).getBytes());
    }
}
