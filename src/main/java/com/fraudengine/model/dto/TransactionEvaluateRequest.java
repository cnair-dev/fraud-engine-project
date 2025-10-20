package com.fraudengine.model.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEvaluateRequest {

    @NotBlank(message = "Transaction ID is required")
    private String txnId;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency;

    private String merchantId;
    private String merchantName;
    private String mcc;
    private String deviceId;
    private String ipCountry;
    private Double latitude;
    private Double longitude;

    @NotBlank(message = "Timestamp is required")
    private String timestamp;

    // Derived aggregates (optional)
    private Double avgAmount;
    private Integer recentTxnCount24h;
    private Double recentSpend24h;
}
