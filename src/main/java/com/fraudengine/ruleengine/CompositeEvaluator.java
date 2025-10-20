package com.fraudengine.ruleengine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fraudengine.ruleengine.config.FraudRuleSet;
import com.fraudengine.ruleengine.config.RuleConfig;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class CompositeEvaluator {

    private final FraudRuleSet ruleSet;
    private final Map<String, RuleConfig> byName;
    private final List<Rule> rules;
    private final ObjectMapper mapper;

    public EvaluationResult evaluate(RuleContext ctx) {
        double total = 0d;
        List<String> reasons = new ArrayList<>();
        ObjectNode debug = JsonNodeFactory.instance.objectNode();

        for (Rule rule : rules) {
            RuleResult rr = rule.evaluate(ctx);
            RuleConfig cfg = byName.get(rule.getName());
            if (cfg == null) continue; // skip if not configured

            double weighted = rr.getScore() * cfg.getWeight() * cfg.getSensitivity();
            total += weighted;
            reasons.add(rr.getReasonCode());

            ObjectNode per = JsonNodeFactory.instance.objectNode();
            per.put("rawScore", rr.getScore());
            per.put("weight", cfg.getWeight());
            per.put("sensitivity", cfg.getSensitivity());
            per.put("weighted", weighted);
            if (rr.getDetails() != null) per.set("details", rr.getDetails());
            debug.set(rule.getName(), per);
        }

        boolean flagged = total >= ruleSet.getThreshold();

        return EvaluationResult.builder()
                .compositeScore(total)
                .flagged(flagged)
                .reasons(reasons)
                .details(debug)
                .build();
    }
}
