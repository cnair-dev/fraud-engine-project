package com.fraudengine.ruleengine.rules;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fraudengine.ruleengine.*;

import java.util.Map;
import java.util.Set;

public class MccRiskRule implements Rule {
    @Override public String getName() { return "MCC_RISK"; }

    @Override
    public RuleResult evaluate(RuleContext ctx) {
        String mcc = ctx.getMcc();
        Set<String> wl = ctx.getMccWhitelist();
        Set<String> bl = ctx.getMccBlacklist();
        Map<String, Integer> tiers = ctx.getMccRiskTier();

        double base;
        if (mcc == null) {
            base = 20d;
        } else if (bl != null && bl.contains(mcc)) {
            base = 100d;
        } else if (wl != null && wl.contains(mcc)) {
            base = 0d;
        } else {
            int tier = (tiers != null && tiers.containsKey(mcc)) ? tiers.get(mcc) : 3; // default medium
            base = (tier - 1) * 25d; // 1->0, 5->100
        }

        ObjectNode det = JsonNodeFactory.instance.objectNode();
        det.put("mcc", mcc == null ? "UNKNOWN" : mcc);
        det.put("scoreBase", base);

        return RuleResult.builder()
                .reasonCode("MCC_RISK")
                .score(base)
                .details(det)
                .build();
    }
}
