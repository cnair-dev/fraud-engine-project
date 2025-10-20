package com.fraudengine.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Entity
@Table(name = "customers",
        indexes = {
                @Index(name = "idx_customer_account_type", columnList = "account_type"),
                @Index(name = "idx_customer_risk_segment", columnList = "risk_segment")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_type", nullable = false, length = 50)
    private String accountType;

    @Column(name = "risk_segment", length = 50)
    private String riskSegment;

    @Column(nullable = false, columnDefinition = "timestamp with time zone")
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);

    @Column(name = "historical_chargebacks", nullable = true)
    private Integer historicalChargebacks;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
