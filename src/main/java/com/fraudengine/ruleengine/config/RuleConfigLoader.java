package com.fraudengine.ruleengine.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

public final class RuleConfigLoader {
    private RuleConfigLoader() {}

    public static FraudRuleSet load(ObjectMapper mapper) {
        try (InputStream is = new ClassPathResource("fraud-ruleset.json").getInputStream()) {
            return mapper.readValue(is, FraudRuleSet.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load fraud-ruleset.json", e);
        }
    }
}
