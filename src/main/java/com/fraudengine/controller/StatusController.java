package com.fraudengine.controller;

import com.fraudengine.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/status")
@RequiredArgsConstructor
public class StatusController {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final FlaggedTransactionRepository flaggedTransactionRepository;
    private final RuleSetRepository ruleSetRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("app", "fraud-rule-engine");
        status.put("status", "UP");
        status.put("timestamp", new Date().toString());

        // Repository check — confirm Spring Data context loaded
        try {
            customerRepository.count();
            transactionRepository.count();
            flaggedTransactionRepository.count();
            ruleSetRepository.count();
            status.put("db", "connected");
        } catch (Exception e) {
            status.put("db", "error: " + e.getMessage());
        }

        return ResponseEntity.ok(status);
    }
}
