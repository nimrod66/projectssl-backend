package com.starnet.SslAgency.recruitment.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "offers", indexes = {
        @Index(name = "idx_offer_application", columnList = "application_id"),
        @Index(name = "idx_offer_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    @ToString.Exclude
    private Application application;

    private BigDecimal offeredSalary;
    private String currency;
    private String positionTitle;
    private LocalDate startDate;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @Column(columnDefinition = "TEXT")
    private String conditions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.PENDING;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime offeredAt = LocalDateTime.now();

    private LocalDateTime respondedAt;
    private LocalDateTime expiresAt;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public enum Status {
        PENDING, ACCEPTED, REJECTED, WITHDRAWN, EXPIRED
    }
}
