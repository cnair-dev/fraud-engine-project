package com.fraudengine.ruleengine;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class EvaluationResult {
    double compositeScore;
    boolean flagged;
    @Singular
    List<String> reasons;      // reason codes from rules (in eval order)
    ObjectNode details;        // optional packed debug info (nullable)
}
