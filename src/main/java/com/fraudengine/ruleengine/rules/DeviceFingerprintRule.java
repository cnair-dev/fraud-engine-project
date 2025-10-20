package com.fraudengine.ruleengine.rules;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fraudengine.ruleengine.*;

import java.util.Set;

public class DeviceFingerprintRule implements Rule {
    @Override public String getName() { return "NEW_DEVICE"; }

    @Override
    public RuleResult evaluate(RuleContext ctx) {
        String deviceId = ctx.getDeviceId();
        Set<String> known = ctx.getKnownDeviceIds();

        boolean newDevice = deviceId != null && (known == null || !known.contains(deviceId));

        double amount = (ctx.getAmount() != null) ? ctx.getAmount().doubleValue() : 0d;
        double avg = (ctx.getAvgAmount() != null && ctx.getAvgAmount() > 0) ? ctx.getAvgAmount() : 0.01;
        double ratio = amount / avg;

        double score = 0d;
        if (newDevice && ratio >= 5.0) score = 100d;
        else if (newDevice && ratio >= 3.0) score = 70d;
        else if (newDevice) score = 40d;

        ObjectNode det = JsonNodeFactory.instance.objectNode();
        det.put("deviceId", deviceId == null ? "UNKNOWN" : deviceId);
        det.put("isKnown", !newDevice);
        det.put("amountToAvgRatio", ratio);

        return RuleResult.builder()
                .reasonCode("NEW_DEVICE")
                .score(score)
                .details(det)
                .build();
    }
}
