package com.fraudengine.model.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder
public class TransactionEvaluateResponse {
    double compositeScore;
    boolean flagged;
    String decision;          // Flag or Allow
    List<String> reasonCodes;
    JsonNode details;
}
