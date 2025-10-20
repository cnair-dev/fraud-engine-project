package com.fraudengine.ruleengine.rules;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fraudengine.ruleengine.*;

public class VelocityRule implements Rule {
    @Override public String getName() { return "VELOCITY"; }

    @Override
    public RuleResult evaluate(RuleContext ctx) {
        int count24 = (ctx.getRecentTxnCount24h() != null) ? ctx.getRecentTxnCount24h() : 0;
        double spend24 = (ctx.getRecentSpend24h() != null) ? ctx.getRecentSpend24h() : 0d;

        double score = Math.min(100d, count24 * 10d + spend24 / 200d); // 10 txns => 100; + spend influence

        ObjectNode det = JsonNodeFactory.instance.objectNode();
        det.put("count24h", count24);
        det.put("spend24h", spend24);

        return RuleResult.builder()
                .reasonCode("TXN_VELOCITY")
                .score(score)
                .details(det)
                .build();
    }
}
