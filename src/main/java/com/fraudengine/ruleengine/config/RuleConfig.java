package com.fraudengine.ruleengine.config;

import lombok.Data;

@Data
public class RuleConfig {
    private String name;         // rule key: AMOUNT_SPIKE, VELOCITY, etc
    private double weight;       // 0..1
    private double sensitivity;  // multiplier
    private String reasonCode;   // output code
}
