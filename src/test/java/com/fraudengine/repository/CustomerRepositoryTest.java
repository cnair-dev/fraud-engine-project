package com.fraudengine.repository;

import com.fraudengine.model.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void shouldSaveAndRetrieveCustomer() {
        // given
        Customer saved = customerRepository.save(
                Customer.builder()
                        .accountType("STANDARD")
                        .riskSegment("LOW")
                        .createdAt(OffsetDateTime.now())
                .build()
        );

        // when
        Customer found = customerRepository.findById(saved.getId()).orElse(null);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getAccountType()).isEqualTo("STANDARD");
        assertThat(found.getRiskSegment()).isEqualTo("LOW");
    }

    @Test
    void shouldCountCustomers() {
        long before = customerRepository.count();
        customerRepository.save(Customer.builder()
                .accountType("BUSINESS")
                .riskSegment("MEDIUM")
                .createdAt(OffsetDateTime.now())
                .build());
        long after = customerRepository.count();

        assertThat(after).isEqualTo(before + 1);
    }
}
