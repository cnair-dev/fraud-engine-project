package com.fraudengine.service;

import com.fraudengine.model.dto.TransactionEvaluateRequest;
import com.fraudengine.model.dto.TransactionEvaluateResponse;
import com.fraudengine.model.entity.Customer;
import com.fraudengine.model.entity.FlaggedTransaction;
import com.fraudengine.model.entity.Transaction;
import com.fraudengine.repository.CustomerRepository;
import com.fraudengine.repository.FlaggedTransactionRepository;
import com.fraudengine.repository.TransactionRepository;
import com.fraudengine.ruleengine.*;
import com.fraudengine.config.ReferenceDataConfig;
import com.fraudengine.ruleengine.mapping.RuleContextMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TransactionEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(TransactionEvaluationService.class);

    private final CompositeEvaluator evaluator;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final FlaggedTransactionRepository flaggedRepository;
    private final ReferenceDataConfig referenceData;
    private final ObjectMapper mapper;
    private final RuleContextMapper contextMapper;

    @Transactional
    public TransactionEvaluateResponse evaluate(TransactionEvaluateRequest req) {
        // --- MDC Context Setup ---
        MDC.put("txnId", req.getTxnId());
        MDC.put("customerId", req.getCustomerId());
        log.info("Starting evaluation for transaction {}", req.getTxnId());

        try {
            UUID customerId = UUID.fromString(req.getCustomerId());
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

            Transaction last = transactionRepository
                    .findTop50ByCustomerIdOrderByTimestampDesc(customerId)
                    .stream().findFirst().orElse(null);

            RuleContext ctx = contextMapper.toRuleContext(req, customer, last, referenceData);
            EvaluationResult result = evaluator.evaluate(ctx);

            String decision = (result.getCompositeScore() < 30) ? "APPROVE"
                    : (result.getCompositeScore() < 65) ? "REVIEW" : "DECLINE";

            log.info("Composite score={} decision={} reasons={}",
                    result.getCompositeScore(), decision, result.getReasons());

            if (result.isFlagged()) {
                FlaggedTransaction flag = FlaggedTransaction.builder()
                        .txnId(UUID.fromString(req.getTxnId()))
                        .customerId(customerId)
                        .score((int) Math.round(result.getCompositeScore()))
                        .decision(decision)
                        .reasonCodes(mapper.valueToTree(result.getReasons()))
                        .details(mapper.valueToTree(result.getDetails()))
                        .txnSnapshot(mapper.valueToTree(req))
                        .build();
                flaggedRepository.save(flag);
                log.info("Flagged transaction persisted with score={} decision={}", result.getCompositeScore(), decision);
            }

            return TransactionEvaluateResponse.builder()
                    .compositeScore(result.getCompositeScore())
                    .flagged(result.isFlagged())
                    .decision(decision)
                    .reasonCodes(result.getReasons())
                    .details(result.getDetails())
                    .build();

        } finally {
            MDC.clear(); // ensure no context leakage
        }
    }

    @Transactional
    public List<TransactionEvaluateResponse> evaluateBatch(List<TransactionEvaluateRequest> requests) {
        List<TransactionEvaluateResponse> results = new ArrayList<>(requests.size());
        for (TransactionEvaluateRequest r : requests) {
            results.add(evaluate(r));
        }
        return results;
    }
}
