package com.fraudengine.repository;

import com.fraudengine.model.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findTop50ByCustomerIdOrderByTimestampDesc(UUID customerId);

    @Query("SELECT AVG(t.amount) FROM Transaction t WHERE t.customer.id = :customerId")
    Double findAvgAmountByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.customer.id = :customerId AND t.timestamp > :since")
    Long countRecentTransactions(@Param("customerId") UUID customerId, @Param("since") Instant since);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.customer.id = :customerId AND t.timestamp > :since")
    BigDecimal sumRecentSpend(@Param("customerId") UUID customerId, @Param("since") Instant since);

    @Query("SELECT t FROM Transaction t WHERE t.customer.id = :customerId ORDER BY t.timestamp DESC")
    List<Transaction> findAllByCustomerOrderDesc(@Param("customerId") UUID customerId);
}
