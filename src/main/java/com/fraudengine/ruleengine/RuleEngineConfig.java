package com.fraudengine.ruleengine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudengine.repository.*;
import com.fraudengine.ruleengine.config.*;
import com.fraudengine.ruleengine.rules.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
public class RuleEngineConfig {

    // Load the rule set from JSON
    @Bean
    public FraudRuleSet fraudRuleSet(ObjectMapper mapper) {
        return RuleConfigLoader.load(mapper);
    }

    // Map rule names to their configs for easy lookup
    @Bean
    public Map<String, RuleConfig> ruleConfigByName(FraudRuleSet set) {
        if (set == null || set.getRules() == null) {
            return Collections.emptyMap();
        }
        return set.getRules().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        r -> r.getName().toUpperCase(Locale.ROOT),
                        r -> r,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    // Instantiate active rule beans with configs injected
    @Bean
    public List<Rule> activeRules(
            TransactionRepository transactionRepository,
            CustomerRepository customerRepository,
            Map<String, RuleConfig> ruleConfigByName
    ) {
        return List.of(
                new HighAmountRule(
                        transactionRepository,
                        customerRepository,
                        ruleConfigByName.get("AMOUNT_SPIKE")
                ),
                new VelocityRule(),
                new MccRiskRule(),
                new GeoVelocityRule(),
                new DeviceFingerprintRule(),
                new MerchantReputationRule(),
                new AccountTenureRule(),
                new TimeOfDayRule()
        );
    }

    // Composite evaluator wiring
    @Bean
    public CompositeEvaluator compositeEvaluator(
            FraudRuleSet set,
            Map<String, RuleConfig> byName,
            List<Rule> rules,
            ObjectMapper mapper
    ) {
        return new CompositeEvaluator(set, byName, rules, mapper);
    }
}
