package com.fraudengine.ruleengine;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RuleEngineServiceSmokeTest {

    @Autowired
    RuleEngineService service;

    @Test
    void evaluatesCompositeScoreAndFlag() {
        RuleContext ctx = RuleContext.builder()
                .txnId("T1")
                .amount(BigDecimal.valueOf(12000))
                .currency("USD")
                .merchantId("m-1")
                .mcc("7995")
                .txnTime(Instant.now())
                .deviceId("d-new")
                .ipCountry("ZA")
                .latitude(-33.9).longitude(18.4)
                .customerId("c-1")
                .accountType("STANDARD")
                .riskSegment("LOW")
                .accountCreatedAt(Instant.now().minusSeconds(10 * 24 * 3600L))
                .historicalChargebacks(1)
                .avgAmount(1200.0)
                .recentTxnCount24h(6)
                .recentSpend24h(3000.0)
                .lastTxnLat(-26.2).lastTxnLon(28.0)
                .lastTxnTime(Instant.now().minusSeconds(3600))
                .knownDeviceIds(Set.of("d-older-1"))
                .merchantReputation(Map.of("m-1", 65))
                .mccRiskTier(Map.of("7995", 5))
                .build();

        EvaluationResult res = service.evaluate(ctx);
        assertThat(res.getCompositeScore()).isGreaterThan(0);
        System.out.println(res.getCompositeScore());
        System.out.println(res.getDetails());
        System.out.println(res.getReasons());
        assertThat(res.getReasons()).isNotEmpty();
        // may or may not be flagged depending on config; the point is: engine runs cleanly
    }
}
