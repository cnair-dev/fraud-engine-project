package com.fraudengine.ruleengine.rules;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fraudengine.ruleengine.*;

import java.time.Duration;

public class GeoVelocityRule implements Rule {
    @Override public String getName() { return "GEO_VELOCITY"; }

    @Override
    public RuleResult evaluate(RuleContext ctx) {
        if (ctx.getLatitude() == null || ctx.getLongitude() == null ||
                ctx.getLastTxnLat() == null || ctx.getLastTxnLon() == null ||
                ctx.getLastTxnTime() == null || ctx.getTxnTime() == null) {

            return RuleResult.builder()
                    .reasonCode("GEO_VELOCITY")
                    .score(0d)
                    .details(null)
                    .build();
        }

        double distKm = haversine(ctx.getLastTxnLat(), ctx.getLastTxnLon(), ctx.getLatitude(), ctx.getLongitude());
        long minutes = Math.max(1, Duration.between(ctx.getLastTxnTime(), ctx.getTxnTime()).toMinutes());
        double speedKmPerH = (distKm / minutes) * 60.0;

        double score = 0d;
        if (distKm > 500 && minutes < 120) score = 100d;
        else if (speedKmPerH > 800) score = 90d;
        else if (speedKmPerH > 400) score = 70d;
        else if (speedKmPerH > 200) score = 40d;

        ObjectNode det = JsonNodeFactory.instance.objectNode();
        det.put("distanceKm", distKm);
        det.put("minutesSinceLast", minutes);
        det.put("speedKmPerH", speedKmPerH);

        return RuleResult.builder()
                .reasonCode("GEO_VELOCITY")
                .score(score)
                .details(det)
                .build();
    }

    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(dLat/2),2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.pow(Math.sin(dLon/2),2);
        return 2 * R * Math.asin(Math.sqrt(a));
    }
}
