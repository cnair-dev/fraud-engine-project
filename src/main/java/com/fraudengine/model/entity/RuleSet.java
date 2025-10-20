package com.fraudengine.model.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "rule_sets",
        uniqueConstraints = @UniqueConstraint(columnNames = {"account_type", "version"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleSet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_type", nullable = false, length = 50)
    private String accountType;

    @Column(nullable = false, length = 20)
    private String version;

    @Column(name = "effective_from", columnDefinition = "timestamp with time zone")
    private OffsetDateTime effectiveFrom;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode config;
}
