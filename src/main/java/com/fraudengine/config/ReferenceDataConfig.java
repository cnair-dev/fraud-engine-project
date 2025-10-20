package com.fraudengine.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Configuration
public class ReferenceDataConfig {

    private static final String DEFAULT_MCC_FILE = "reference-data/mcc-tiers.json";
    private static final String DEFAULT_MERCHANT_FILE = "reference-data/merchant-lists.json";
    private static final String DEFAULT_REPUTATION_FILE = "reference-data/merchant-reputation.json";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public record MerchantLists(Set<String> whitelist, Set<String> blacklist) {}

    /** MCC risk tiers: code → risk tier (1–5) */
    @Bean
    public Map<String, Integer> mccTierMap() {
        return loadJson(DEFAULT_MCC_FILE, new TypeReference<Map<String, Integer>>() {});
    }

    /** Merchant whitelist / blacklist */
    @Bean
    public MerchantLists merchantLists() {
        return loadJson(DEFAULT_MERCHANT_FILE, new TypeReference<MerchantLists>() {});
    }

    /** Merchant reputation: merchantId → score 0–100 */
    @Bean
    public Map<String, Integer> merchantReputationMap() {
        return loadJson(DEFAULT_REPUTATION_FILE, new TypeReference<Map<String, Integer>>() {});
    }

    private <T> T loadJson(String path, TypeReference<T> typeRef) {
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            return objectMapper.readValue(is, typeRef);
        } catch (Exception e) {
            // Defensive fallback so engine still runs
            if (typeRef.getType().getTypeName().contains("Map"))
                return (T) Collections.emptyMap();
            if (typeRef.getType().getTypeName().contains("MerchantLists"))
                return (T) new MerchantLists(Collections.emptySet(), Collections.emptySet());
            throw new IllegalStateException("Failed to load reference data: " + path, e);
        }
    }
}
