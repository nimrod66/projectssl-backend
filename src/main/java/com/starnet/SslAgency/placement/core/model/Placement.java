package com.starnet.SslAgency.placement.core.model;

import com.starnet.SslAgency.applicant.model.Applicant;
import com.starnet.SslAgency.contract.model.Contract;
import com.starnet.SslAgency.employer.model.Employer;
import com.starnet.SslAgency.opportunity.model.Opportunity;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.recruitment.model.Application;
import com.starnet.SslAgency.recruitment.model.Offer;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "placements", indexes = {
        @Index(name = "idx_placement_applicant", columnList = "applicant_id"),
        @Index(name = "idx_placement_stage", columnList = "stage"),
        @Index(name = "idx_placement_application", columnList = "application_id", unique = true),
        @Index(name = "idx_placement_opportunity", columnList = "opportunity_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Placement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String placementNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    @ToString.Exclude
    private Applicant applicant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    @ToString.Exclude
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accepted_offer_id", nullable = false)
    @ToString.Exclude
    private Offer acceptedOffer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false)
    @ToString.Exclude
    private Opportunity opportunity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employer_id", nullable = false)
    @ToString.Exclude
    private Employer employer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    @ToString.Exclude
    private Contract contract;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    @Builder.Default
    private Stage stage = Stage.CREATED;

    private LocalDate startDate;
    private LocalDate expectedEndDate;

    @Column(name = "termination_reason", columnDefinition = "TEXT")
    private String terminationReason;

    @Column(name = "return_reason", columnDefinition = "TEXT")
    private String returnReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Staff createdBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "placement", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<PlacementStatusHistory> history = new ArrayList<>();

    @OneToMany(mappedBy = "placement", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<PlacementChecklist> checklist = new ArrayList<>();

    public boolean isActive() {
        return stage != Stage.COMPLETED && stage != Stage.TERMINATED && stage != Stage.RETURNED;
    }

    public enum Stage {
        CREATED, DOCUMENTATION, MEDICAL, VISA, CONTRACT_SIGNED,
        TRAVEL_READY, DEPLOYED, COMPLETED, TERMINATED, RETURNED
    }
}