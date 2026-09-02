package com.starnet.SslAgency.recruitment.model;

import com.starnet.SslAgency.applicant.model.Applicant;
import com.starnet.SslAgency.opportunity.model.Opportunity;
import com.starnet.SslAgency.processor.model.Staff;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recruitment_applications", indexes = {
        @Index(name = "idx_recruitment_application_applicant", columnList = "applicant_id"),
        @Index(name = "idx_recruitment_application_opportunity", columnList = "opportunity_id"),
        @Index(name = "idx_recruitment_application_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Applicant applicant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_recruiter_id")
    private Staff assignedRecruiter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    @Builder.Default
    private Status status = Status.SUBMITTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "rejection_reason", length = 40)
    private RejectionReason rejectionReason;

    @Column(name = "rejection_details", columnDefinition = "TEXT")
    private String rejectionDetails;

    @Column(name = "applied_at", nullable = false)
    @Builder.Default
    private LocalDateTime appliedAt = LocalDateTime.now();

    @Column(name = "last_activity_at", nullable = false)
    @Builder.Default
    private LocalDateTime lastActivityAt = LocalDateTime.now();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("scheduledAt ASC")
    @Builder.Default
    @ToString.Exclude
    private List<Interview> interviews = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("offeredAt DESC")
    @Builder.Default
    @ToString.Exclude
    private List<Offer> offers = new ArrayList<>();

    public enum Status {
        SUBMITTED, SCREENING, SHORTLISTED, INTERVIEW, OFFERED, ACCEPTED, PLACED, REJECTED, WITHDRAWN
    }

    public enum RejectionReason {
        MISSING_QUALIFICATION,
        FAILED_SCREENING,
        FAILED_INTERVIEW,
        EMPLOYER_REJECTION,
        DOCUMENTS_INVALID,
        SALARY_MISMATCH,
        POSITION_FILLED,
        CANDIDATE_WITHDREW,
        OTHER
    }
}
