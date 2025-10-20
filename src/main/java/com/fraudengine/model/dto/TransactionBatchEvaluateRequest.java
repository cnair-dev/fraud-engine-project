package com.fraudengine.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionBatchEvaluateRequest {

    @NotEmpty(message = "Transactions list must not be empty")
    @Valid // ensures nested TransactionEvaluateRequest objects are validated too
    private List<TransactionEvaluateRequest> transactions;
}
