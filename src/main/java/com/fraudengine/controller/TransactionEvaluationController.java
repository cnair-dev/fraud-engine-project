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
            summary = "Retrieve flagged transactions (paginated)",
            description = """
            Returns all transactions that were flagged by the fraud engine.
            Supports optional filters for customer, decision band, score range, date window, and reason codes.
            Use `includeSnapshot=true` to include the original evaluated transaction payload (txnSnapshot).
            """,
            parameters = {
                    @Parameter(
                            name = "customerId",
                            description = "Customer UUID to filter by",
                            required = true,
                            example = "11111111-2222-3333-4444-555555555555"
                    ),
                    @Parameter(
                            name = "decision",
                            description = "Filter by decision band (APPROVE, REVIEW, DECLINE)",
                            example = "REVIEW"
                    ),
                    @Parameter(
                            name = "minScore",
                            description = "Minimum composite score threshold",
                            example = "40"
                    ),
                    @Parameter(
                            name = "from",
                            description = "Start of date range (ISO-8601)",
                            example = "2025-10-19T00:00:00Z"
                    ),
                    @Parameter(
                            name = "to",
                            description = "End of date range (ISO-8601)",
                            example = "2025-10-20T00:00:00Z"
                    ),
                    @Parameter(
                            name = "reason",
                            description = "Optional reason code to filter (e.g., AMOUNT_SPIKE, MCC_RISK)",
                            example = "AMOUNT_SPIKE"
                    ),
                    @Parameter(
                            name = "page",
                            description = "Page number (default 0)",
                            example = "0"
                    ),
                    @Parameter(
                            name = "size",
                            description = "Page size (default 10)",
                            example = "10"
                    ),
                    @Parameter(
                            name = "includeSnapshot",
                            description = "Include full transaction snapshot JSON in each flagged record",
                            example = "true"
                    )
            }
    )
    public ResponseEntity<Page<FlaggedTransaction>> getFlags(
            @RequestParam("customerId") UUID customerId,
            @RequestParam(required = false) String decision,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) String reason,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "true") boolean includeSnapshot
    ) {
        Page<FlaggedTransaction> flags = flaggedRepository.searchFlags(
                customerId, decision, minScore, from, to, reason, PageRequest.of(page, size)
        );

        if (!includeSnapshot) {
            flags.forEach(f -> f.setTxnSnapshot(null));
        }

        return ResponseEntity.ok(flags);
    }
}
