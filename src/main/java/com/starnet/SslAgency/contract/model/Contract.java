package com.starnet.SslAgency.contract.model;

import com.starnet.SslAgency.employer.model.Employer;
import com.starnet.SslAgency.placement.model.Placement;
import com.starnet.SslAgency.processor.model.Staff;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contracts")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private Employer employer;

    @Column(nullable = false)
    private String jobCategory;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private int numberOfPositions;

    @Builder.Default
    private int filledPositions = 0;

    @Column(nullable = false)
    private BigDecimal salary;

    @Builder.Default
    private String currency = "USD";

    private int durationMonths;

    private LocalDate startDate;
    private LocalDate endDate;

    @Builder.Default
    private boolean renewable = false;

    @Column(columnDefinition = "TEXT")
    private String termsAndConditions;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    private String workingHours;

    @Builder.Default
    private boolean accommodationProvided = false;

    @Builder.Default
    private boolean transportProvided = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Staff createdBy;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Placement> placements = new ArrayList<>();

    public enum Status {
        OPEN, FILLED, CLOSED
    }
}
