package com.fraudengine.repository;

import com.fraudengine.model.entity.RuleSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RuleSetRepositoryTest {

    @Autowired
    private RuleSetRepository ruleSetRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldSaveAndRetrieveRuleSetConfig() {
        // given
        Map<String, Object> config = Map.of(
                "rules", Map.of(
                        "AMOUNT_SPIKE", Map.of("weight", 0.5, "threshold", 5000),
                        "VELOCITY", Map.of("weight", 0.3, "threshold", 10)
                ),
                "decisionBands", Map.of(
                        "APPROVE", "<30",
                        "REVIEW", "30-70",
                        "DECLINE", ">70"
                )
        );

        RuleSet saved = ruleSetRepository.save(RuleSet.builder()
                .accountType("STANDARD")
                .version("v1")
                .effectiveFrom(OffsetDateTime.now())
                .config(objectMapper.valueToTree(config))
                .build());

        // when
        RuleSet found = ruleSetRepository.findById(saved.getId()).orElse(null);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getAccountType()).isEqualTo("STANDARD");
        assertThat(found.getConfig().get("rules").get("AMOUNT_SPIKE").get("threshold").asInt()).isEqualTo(5000);
    }
}
