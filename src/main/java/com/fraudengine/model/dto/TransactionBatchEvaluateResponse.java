package com.fraudengine.model.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class TransactionBatchEvaluateResponse {
    List<TransactionEvaluateResponse> results;
}
