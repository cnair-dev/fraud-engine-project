package com.fraudengine.ruleengine;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RuleResult {
    String reasonCode;       // e.g. "AMOUNT_SPIKE"
    double score;            // 0..100
    ObjectNode details;      // optional structured info (nullable)
}
