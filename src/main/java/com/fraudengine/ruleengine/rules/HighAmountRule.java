package com.fraudengine.ruleengine.rules;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fraudengine.model.entity.Customer;
import com.fraudengine.repository.CustomerRepository;
import com.fraudengine.repository.TransactionRepository;
import com.fraudengine.ruleengine.*;
import com.fraudengine.ruleengine.config.RuleConfig;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
public class HighAmountRule implements Rule {

    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final RuleConfig ruleConfig;

    @Override
    public String getName() {
        return "AMOUNT_SPIKE";
    }

    @Override
    public RuleResult evaluate(RuleContext ctx) {
        BigDecimal amountBD = ctx.getAmount();
        String custIdStr = ctx.getCustomerId();

        if (amountBD == null || custIdStr == null) {
            return RuleResult.builder()
                    .reasonCode("AMOUNT_SPIKE")
                    .score(0.0)
                    .details(JsonNodeFactory.instance.objectNode()
                            .put("error", "missing_amount_or_customer"))
                    .build();
        }

        UUID customerId;
        try {
            customerId = UUID.fromString(custIdStr);
        } catch (IllegalArgumentException e) {
            return RuleResult.builder()
                    .reasonCode("AMOUNT_SPIKE")
                    .score(0.0)
                    .details(JsonNodeFactory.instance.objectNode()
                            .put("error", "invalid_customer_id"))
                    .build();
        }

        double amount = amountBD.doubleValue();
        Customer customer = customerRepository.findById(customerId).orElse(null);
        String acctType = (customer != null && customer.getAccountType() != null)
                ? customer.getAccountType().toUpperCase()
                : "STANDARD";

        double threshold = switch (acctType) {
            case "PREMIUM" -> 15000.0;
            case "BUSINESS" -> 20000.0;
            default -> 5000.0;
        };

        Double avgObj = transactionRepository.findAvgAmountByCustomerId(customerId);
        double avgAmount = (avgObj == null || avgObj <= 0.0) ? 1000.0 : avgObj;

        Double sensitivityObj = (ruleConfig != null) ? ruleConfig.getSensitivity() : null;
        double sensitivity = (sensitivityObj == null) ? 1.0 : sensitivityObj;

        double rawScore = 0.0;
        if (amount >= threshold) {
            double ratioAvg = amount / avgAmount;
            double ratioThr = amount / threshold;

            double hybrid = 0.6 * Math.log10(Math.max(ratioAvg, 1.0))
                    + 0.3 * Math.log10(Math.max(ratioThr, 1.0));

            rawScore = Math.min(100.0, hybrid * 60.0 * sensitivity);
        }

        ObjectNode details = JsonNodeFactory.instance.objectNode();
        details.put("amount", amount);
        details.put("avgAmount", avgAmount);
        details.put("threshold", threshold);
        details.put("accountType", acctType);
        details.put("sensitivity", sensitivity);
        details.put("scoreFormula", "0.6*log10(amount/avg)+0.3*log10(amount/threshold)");
        details.put("ratioAvg", amount / avgAmount);
        details.put("ratioThreshold", amount / threshold);

        return RuleResult.builder()
                .reasonCode("AMOUNT_SPIKE")
                .score(rawScore)
                .details(details)
                .build();
    }
}
