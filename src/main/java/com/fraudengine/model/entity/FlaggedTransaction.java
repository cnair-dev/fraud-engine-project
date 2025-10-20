package com.fraudengine.model.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import org.hibernate.type.SqlTypes;


@Entity
@Table(name = "flagged_transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlaggedTransaction {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "txn_id", nullable = false)
    private UUID txnId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false, length = 20)
    private String decision;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode reasonCodes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode details;

    @Column(nullable = false, columnDefinition = "timestamp with time zone")
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode txnSnapshot;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
