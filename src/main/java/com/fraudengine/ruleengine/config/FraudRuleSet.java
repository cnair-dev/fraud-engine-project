package com.fraudengine.ruleengine.config;

import lombok.Data;

import java.util.List;

@Data
public class FraudRuleSet {
    private String version;
    private String effectiveFrom;
    private Scoring scoring;
    private double threshold;
    private List<RuleConfig> rules;

    @Data
    public static class Scoring {
        private String composite;
        private String flag_logic;
    }
}
