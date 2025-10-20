package com.fraudengine.ruleengine;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Minimal, engine-friendly context. Populate what you have; rules handle nulls safely.
 */
@Value
@Builder
public class RuleContext {
    // Transaction-level
    String txnId;
    BigDecimal amount;
    String currency;
    String merchantId;
    String merchantName;
    String mcc;                 // 4-digit Merchant Category Code
    Instant txnTime;
    String deviceId;
    String ipCountry;           // ISO country (e.g. "ZA")
    Double latitude;            // nullable
    Double longitude;           // nullable

    // Customer-level
    String customerId;
    String accountType;         // e.g. STANDARD/BUSINESS
    String riskSegment;         // LOW/MEDIUM/HIGH
    Instant accountCreatedAt;
    Integer historicalChargebacks;     // count (nullable)

    // Aggregates / recent behaviour (from DB/analytics)
    Double avgAmount;                 // avg ticket
    Integer recentTxnCount24h;        // last 24h count
    Double recentSpend24h;            // last 24h spend
    Instant lastTxnTime;              // previous txn time
    Double lastTxnLat;                // previous txn lat
    Double lastTxnLon;                // previous txn lon

    // Known signals / lists
    Set<String> knownDeviceIds;       // devices we've seen
    Set<String> merchantWhitelist;    // merchant ids
    Set<String> merchantBlacklist;    // merchant ids
    Set<String> mccWhitelist;         // MCC codes
    Set<String> mccBlacklist;         // MCC codes

    // Reputation inputs (0..100 high is worse)
    Map<String, Integer> merchantReputation;  // merchantId -> 0..100
    Map<String, Integer> mccRiskTier;         // MCC -> 1..5 tier (5 worst)
}
