package com.fraudengine.ruleengine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RuleEngineService {
    private final CompositeEvaluator evaluator;

    public EvaluationResult evaluate(RuleContext ctx) {
        return evaluator.evaluate(ctx);
    }
}
