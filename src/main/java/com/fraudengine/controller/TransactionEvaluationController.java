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
            summary = "Retrieve flagged transactions",
            description = """
            Returns a paginated list of flagged transactions for a given customer.
            Supports filtering by decision, score, date range, and reason code.
            Useful for audit trails and analyst dashboards.
            """,
            parameters = {
                    @Parameter(
                            name = "customerId",
                            required = true,
                            description = "UUID of the customer whose flagged transactions should be retrieved",
                            example = "11111111-2222-3333-4444-555555555555"
                    ),
                    @Parameter(
                            name = "decision",
                            description = "Filter by decision outcome (APPROVE, REVIEW, DECLINE)",
                            example = "REVIEW"
                    ),
                    @Parameter(
                            name = "minScore",
                            description = "Only include transactions with a score greater than or equal to this value",
                            example = "60"
                    ),
                    @Parameter(
                            name = "from",
                            description = "Start of time range (ISO-8601)",
                            example = "2025-10-15T00:00:00Z"
                    ),
                    @Parameter(
                            name = "to",
                            description = "End of time range (ISO-8601)",
                            example = "2025-10-20T23:59:59Z"
                    ),
                    @Parameter(
                            name = "reason",
                            description = "Reason code to filter by (e.g. AMOUNT_SPIKE)",
                            example = "AMOUNT_SPIKE"
                    ),
                    @Parameter(
                            name = "page",
                            description = "Page number (0-indexed, default = 0)",
                            example = "0"
                    ),
                    @Parameter(
                            name = "size",
                            description = "Page size (default = 10)",
                            example = "10"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Paginated list of flagged transactions retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                        {
                                          "content": [
                                            {
                                              "id": "f1e2d3c4-b5a6-7890-abcd-ef1234567890",
                                              "txnId": "b1a2d3c4-e5f6-7890-abcd-ef1234567890",
                                              "customerId": "11111111-2222-3333-4444-555555555555",
                                              "score": 82.5,
                                              "decision": "DECLINE",
                                              "reasonCodes": ["AMOUNT_SPIKE", "MCC_RISK"],
                                              "details": {
                                                "AMOUNT_SPIKE": {"ratio": 1.35},
                                                "MCC_RISK": {"mcc": "7995"}
                                              },
                                              "createdAt": "2025-10-19T15:23:00Z"
                                            }
                                          ],
                                          "pageable": { "pageNumber": 0, "pageSize": 10 },
                                          "totalElements": 2,
                                          "totalPages": 1,
                                          "last": true,
                                          "first": true
                                        }
                                        """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid query parameters or malformed UUID",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                        {
                                          "error": "Invalid customerId format",
                                          "timestamp": "2025-10-20T08:22:10Z"
                                        }
                                        """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No flagged transactions found for the given filters",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                        {
                                          "error": "No flagged transactions found",
                                          "timestamp": "2025-10-20T08:25:00Z"
                                        }
                                        """)
                            )
                    )
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
