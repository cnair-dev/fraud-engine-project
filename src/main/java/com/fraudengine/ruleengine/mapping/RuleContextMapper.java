package com.fraudengine.ruleengine.mapping;

import com.fraudengine.config.ReferenceDataConfig;
import com.fraudengine.model.dto.TransactionEvaluateRequest;
import com.fraudengine.model.entity.Customer;
import com.fraudengine.model.entity.Transaction;
import com.fraudengine.ruleengine.RuleContext;
import org.mapstruct.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Set;

@Mapper(componentModel = "spring", imports = {Instant.class})
public interface RuleContextMapper {

    @Mappings({
            @Mapping(target = "txnId", source = "req.txnId"),
            @Mapping(target = "amount", source = "req.amount"),
            @Mapping(target = "currency", source = "req.currency"),
            @Mapping(target = "merchantId", source = "req.merchantId"),
            @Mapping(target = "merchantName", source = "req.merchantName"),
            @Mapping(target = "mcc", source = "req.mcc"),
            @Mapping(target = "txnTime", expression = "java(Instant.parse(req.getTimestamp()))"),
            @Mapping(target = "deviceId", source = "req.deviceId"),
            @Mapping(target = "ipCountry", source = "req.ipCountry"),
            @Mapping(target = "latitude", source = "req.latitude"),
            @Mapping(target = "longitude", source = "req.longitude"),

            @Mapping(target = "customerId", source = "req.customerId"),
            @Mapping(target = "accountType", source = "customer.accountType"),
            @Mapping(target = "riskSegment", source = "customer.riskSegment"),
            @Mapping(target = "accountCreatedAt", source = "customer.createdAt"),
            @Mapping(target = "historicalChargebacks", source = "customer.historicalChargebacks"),

            @Mapping(target = "avgAmount", source = "req.avgAmount"),
            @Mapping(target = "recentTxnCount24h", source = "req.recentTxnCount24h"),
            @Mapping(target = "recentSpend24h", source = "req.recentSpend24h"),
            @Mapping(target = "lastTxnTime", expression = "java(last != null && last.getTimestamp() != null ? last.getTimestamp().toInstant() : null)"),
            @Mapping(target = "lastTxnLat", expression = "java(last != null ? last.getLocationLat() : null)"),
            @Mapping(target = "lastTxnLon", expression = "java(last != null ? last.getLocationLon() : null)"),

            // reference data filled in @AfterMapping
            @Mapping(target = "knownDeviceIds", ignore = true),
            @Mapping(target = "merchantWhitelist", ignore = true),
            @Mapping(target = "merchantBlacklist", ignore = true),
            @Mapping(target = "mccWhitelist", ignore = true),
            @Mapping(target = "mccBlacklist", ignore = true),
            @Mapping(target = "merchantReputation", ignore = true),
            @Mapping(target = "mccRiskTier", ignore = true)
    })
    RuleContext toRuleContext(TransactionEvaluateRequest req, Customer customer, Transaction last, ReferenceDataConfig ref);

    // ✅ Conversion helper for OffsetDateTime → Instant
    default Instant map(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

    @AfterMapping
    default void enrichReferenceData(@MappingTarget RuleContext.RuleContextBuilder builder,
                                     TransactionEvaluateRequest req,
                                     Customer customer,
                                     Transaction last,
                                     ReferenceDataConfig ref) {

        builder.knownDeviceIds(Set.of());

        var merchants = ref.merchantLists();
        builder.merchantWhitelist(merchants.whitelist());
        builder.merchantBlacklist(merchants.blacklist());
        builder.merchantReputation(ref.merchantReputationMap());

        builder.mccRiskTier(ref.mccTierMap());
        builder.mccWhitelist(Set.of());
        builder.mccBlacklist(Set.of());
    }
}
