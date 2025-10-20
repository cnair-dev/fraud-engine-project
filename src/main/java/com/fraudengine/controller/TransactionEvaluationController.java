package com.fraudengine.controller;

import com.fraudengine.model.dto.TransactionBatchEvaluateRequest;
import com.fraudengine.model.dto.TransactionBatchEvaluateResponse;
import com.fraudengine.model.dto.TransactionEvaluateRequest;
import com.fraudengine.model.dto.TransactionEvaluateResponse;
import com.fraudengine.model.entity.FlaggedTransaction;
import com.fraudengine.repository.FlaggedTransactionRepository;
import com.fraudengine.service.TransactionEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionEvaluationController {

    private final TransactionEvaluationService service;
    private final FlaggedTransactionRepository flaggedRepository;

    @PostMapping("/evaluate")
    @Operation(
            summary = "Evaluate a single transaction for fraud risk",
            description = "Evaluates a single transaction and returns a fraud score, decision, and reason codes.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = TransactionEvaluateRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Example Transaction",
                                            value = """
                                            {
                                              "txnId": "b1a2d3c4-e5f6-7890-abcd-ef1234567890",
                                              "customerId": "11111111-2222-3333-4444-555555555555",
                                              "amount": 9500,
                                              "currency": "ZAR",
                                              "merchantId": "11111111-aaaa-bbbb-cccc-111111111111",
                                              "mcc": "5812",
                                              "timestamp": "2025-10-19T14:25:00Z"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Transaction evaluated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                            {
                                              "compositeScore": 47.7,
                                              "decision": "REVIEW",
                                              "reasonCodes": ["AMOUNT_SPIKE", "MCC_RISK"]
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid input or validation error")
            }
    )
    public ResponseEntity<TransactionEvaluateResponse> evaluate(
            @Valid @org.springframework.web.bind.annotation.RequestBody TransactionEvaluateRequest request) {
        TransactionEvaluateResponse response = service.evaluate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch-evaluate")
    @Operation(
            summary = "Evaluate a batch of transactions",
            description = "Evaluates multiple transactions in one request. Returns an array of results with scores, decisions, and reason codes.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = TransactionBatchEvaluateRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Example Batch Evaluation",
                                            value = """
                                            {
                                              "transactions": [
                                                {
                                                  "txnId": "11111111-aaaa-bbbb-cccc-999999999999",
                                                  "customerId": "11111111-2222-3333-4444-555555555555",
                                                  "amount": 8000,
                                                  "currency": "ZAR",
                                                  "merchantId": "44444444-aaaa-bbbb-cccc-444444444444",
                                                  "mcc": "5812",
                                                  "timestamp": "2025-10-19T15:25:00Z"
                                                },
                                                {
                                                  "txnId": "22222222-aaaa-bbbb-cccc-999999999999",
                                                  "customerId": "33333333-4444-5555-6666-777777777777",
                                                  "amount": 12000,
                                                  "currency": "ZAR",
                                                  "merchantId": "33333333-aaaa-bbbb-cccc-333333333333",
                                                  "mcc": "7995",
                                                  "timestamp": "2025-10-19T16:00:00Z"
                                                }
                                              ]
                                            }
                                            """
                                    )
                            }
                    )
            )
    )
    public ResponseEntity<TransactionBatchEvaluateResponse> evaluateBatch(
            @Valid @org.springframework.web.bind.annotation.RequestBody TransactionBatchEvaluateRequest request) {
        List<TransactionEvaluateResponse> results = service.evaluateBatch(request.getTransactions());
        return ResponseEntity.ok(TransactionBatchEvaluateResponse.builder().results(results).build());
    }

    @GetMapping("/flags")
    @Operation(
            summary = "List flagged transactions for a customer (paginated + filters)",
            description = "Retrieve persisted flagged transactions filtered by customer, decision, score threshold, date range, and reason code.",
            parameters = {
                    @Parameter(name = "customerId", required = true, description = "Customer UUID"),
                    @Parameter(name = "decision", description = "APPROVE, REVIEW, DECLINE"),
                    @Parameter(name = "minScore", description = "Minimum composite score"),
                    @Parameter(name = "from", description = "Start (ISO-8601)"),
                    @Parameter(name = "to", description = "End (ISO-8601)"),
                    @Parameter(name = "reason", description = "Reason code filter, e.g. AMOUNT_SPIKE"),
                    @Parameter(name = "page", description = "Page number"),
                    @Parameter(name = "size", description = "Page size")
            }
    )
    public ResponseEntity<Page<FlaggedTransaction>> getFlags(
            @RequestParam("customerId") UUID customerId,
            @RequestParam(required = false) String decision,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) String reason,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<FlaggedTransaction> flags = flaggedRepository.searchFlags(
                customerId, decision, minScore, from, to, reason, PageRequest.of(page, size)
        );
        return ResponseEntity.ok(flags);
    }
}
