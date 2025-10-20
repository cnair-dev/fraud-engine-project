package com.fraudengine.repository;

import com.fraudengine.model.entity.Customer;
import com.fraudengine.model.entity.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransactionRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void shouldSaveTransactionLinkedToCustomer() {
        Customer customer = customerRepository.save(Customer.builder()
                .accountType("STANDARD")
                .riskSegment("LOW")
                .createdAt(OffsetDateTime.now())
                .build());

        Transaction txn = transactionRepository.save(Transaction.builder()
                .customer(customer)
                .amount(BigDecimal.valueOf(999.99))
                .currency("USD")
                .merchantName("Test Merchant")
                .timestamp(OffsetDateTime.now())
                .category("TEST")
                .build());

        Transaction found = transactionRepository.findById(txn.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getCustomer().getId()).isEqualTo(customer.getId());
        assertThat(found.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(999.99));
    }
}
