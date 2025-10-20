package com.fraudengine.ruleengine.rules;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fraudengine.ruleengine.*;

import java.time.Duration;
import java.time.Instant;

public class AccountTenureRule implements Rule {
    @Override public String getName() { return "ACCOUNT_RISK_HISTORY"; }

    @Override
    public RuleResult evaluate(RuleContext ctx) {
        Instant created = ctx.getAccountCreatedAt();
        long days = (created != null) ? Math.max(0, Duration.between(created, Instant.now()).toDays()) : 0L;

        int cb = (ctx.getHistoricalChargebacks() != null) ? ctx.getHistoricalChargebacks() : 0;

        double score = 0d;
        if (days < 7) score += 60d;
        else if (days < 30) score += 40d;
        else if (days < 90) score += 20d;

        score += Math.min(40d, cb * 15d); // each chargeback adds 15 up to +40

        ObjectNode det = JsonNodeFactory.instance.objectNode();
        det.put("tenureDays", days);
        det.put("chargebacks", cb);

        return RuleResult.builder()
                .reasonCode("ACCOUNT_RISK_HISTORY")
                .score(score)
                .details(det)
                .build();
    }
}
