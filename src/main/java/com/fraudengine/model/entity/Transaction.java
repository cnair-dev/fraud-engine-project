package com.fraudengine.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "transactions",
        indexes = {
                @Index(name = "idx_txn_customer_time", columnList = "customer_id, timestamp")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(length = 3, nullable = false)
    private String currency;

    @Column(nullable = false, columnDefinition = "timestamp with time zone")
    private OffsetDateTime timestamp;

    @Column(name = "merchant_id")
    private UUID merchantId;

    @Column(name = "merchant_name", length = 255)
    private String merchantName;

    @Column(length = 10)
    private String mcc;

    @Column(length = 100)
    private String category;

    @Column(length = 50)
    private String channel;

    @Column(name = "device_id")
    private UUID deviceId;

    @Column(name = "location_lat")
    private Double locationLat;

    @Column(name = "location_lon")
    private Double locationLon;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(length = 500)
    private String description;

    @Column(name = "ip_country")
    private String ipCountry;

    @Column(nullable = false, columnDefinition = "timestamp with time zone")
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        if (timestamp == null) timestamp = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
