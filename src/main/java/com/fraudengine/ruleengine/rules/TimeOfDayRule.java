package com.fraudengine.ruleengine.rules;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fraudengine.ruleengine.*;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class TimeOfDayRule implements Rule {

    @Override
    public String getName() { return "TIME_OF_DAY"; }

    @Override
    public RuleResult evaluate(RuleContext ctx) {
        Instant txnTime = ctx.getTxnTime();
        if (txnTime == null) {
            return RuleResult.builder()
                    .reasonCode("TIME_OF_DAY")
                    .score(0)
                    .details(null)
                    .build();
        }

        ZonedDateTime zdt = txnTime.atZone(ZoneOffset.UTC);
        int hour = zdt.getHour();

        double score = 0d;
        if (hour >= 0 && hour < 4) score = 50d;          // midnight–4am
        else if (hour >= 4 && hour < 6) score = 25d;     // dawn
        else score = 0d;                                 // normal hours

        ObjectNode det = JsonNodeFactory.instance.objectNode();
        det.put("hour", hour);
        det.put("score", score);

        return RuleResult.builder()
                .reasonCode("TIME_OF_DAY")
                .score(score)
                .details(det)
                .build();
    }
}
