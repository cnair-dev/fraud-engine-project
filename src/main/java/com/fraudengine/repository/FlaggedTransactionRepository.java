package com.fraudengine.repository;

import com.fraudengine.model.entity.FlaggedTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface FlaggedTransactionRepository extends JpaRepository<FlaggedTransaction, UUID> {

    Page<FlaggedTransaction> findByCustomerId(UUID customerId, Pageable pageable);

    @Query(
            value = """
            SELECT * FROM flagged_transactions
            WHERE (customer_id = COALESCE(:customerId, customer_id))
              AND (decision   = COALESCE(:decision, decision))
              AND (score      >= COALESCE(:minScore, score))
              AND (created_at >= COALESCE(CAST(:from AS timestamptz), '-infinity'::timestamptz))
              AND (created_at <= COALESCE(CAST(:to   AS timestamptz), 'infinity'::timestamptz))
              AND (:reason IS NULL OR reason_codes @> CAST(CONCAT('["', :reason, '"]') AS jsonb))
            ORDER BY created_at DESC
            """,
            countQuery = """
            SELECT count(*) FROM flagged_transactions
            WHERE (customer_id = COALESCE(:customerId, customer_id))
              AND (decision   = COALESCE(:decision, decision))
              AND (score      >= COALESCE(:minScore, score))
              AND (created_at >= COALESCE(CAST(:from AS timestamptz), '-infinity'::timestamptz))
              AND (created_at <= COALESCE(CAST(:to   AS timestamptz), 'infinity'::timestamptz))
              AND (:reason IS NULL OR reason_codes @> CAST(CONCAT('["', :reason, '"]') AS jsonb))
            """,
            nativeQuery = true
    )
    Page<FlaggedTransaction> searchFlags(
            @Param("customerId") UUID customerId,
            @Param("decision") String decision,
            @Param("minScore") Integer minScore,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            @Param("reason") String reason,
            Pageable pageable
    );
}
