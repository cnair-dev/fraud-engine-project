package com.fraudengine.ruleengine.rules;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fraudengine.ruleengine.*;

import java.util.Map;
import java.util.Set;

public class MerchantReputationRule implements Rule {
    @Override public String getName() { return "MERCHANT_REPUTATION"; }

    @Override
    public RuleResult evaluate(RuleContext ctx) {
        String mid = ctx.getMerchantId();
        Set<String> wl = ctx.getMerchantWhitelist();
        Set<String> bl = ctx.getMerchantBlacklist();
        Map<String, Integer> rep = ctx.getMerchantReputation();

        double score;
        if (mid == null) {
            score = 20d;
        } else if (bl != null && bl.contains(mid)) {
            score = 100d;
        } else if (wl != null && wl.contains(mid)) {
            score = 0d;
        } else {
            int r = (rep != null && rep.containsKey(mid)) ? rep.get(mid) : 50; // default neutral
            score = Math.max(0, Math.min(100, r));
        }

        ObjectNode det = JsonNodeFactory.instance.objectNode();
        det.put("merchantId", mid == null ? "UNKNOWN" : mid);
        det.put("repScore", (mid != null && rep != null && rep.containsKey(mid)) ? rep.get(mid) : 50);

        return RuleResult.builder()
                .reasonCode("MERCHANT_REPUTATION")
                .score(score)
                .details(det)
                .build();
    }
}
