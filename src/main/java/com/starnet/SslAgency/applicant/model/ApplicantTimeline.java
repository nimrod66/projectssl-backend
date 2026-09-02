package com.starnet.SslAgency.applicant.model;

import com.starnet.SslAgency.processor.model.Staff;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "applicant_timeline", indexes = {
        @Index(name = "idx_timeline_applicant", columnList = "applicant_id"),
        @Index(name = "idx_timeline_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicantTimeline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    @ToString.Exclude
    private Applicant applicant;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private EventType eventType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private Staff performedBy;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum EventType {
        REGISTERED,
        PROFILE_UPDATED,
        PROFILE_COMPLETED,
        SUBMITTED_FOR_REVIEW,
        RECRUITER_ASSIGNED,
        DOCUMENT_UPLOADED,
        DOCUMENT_VERIFIED,
        DOCUMENT_REJECTED,
        VETTED,
        ELIGIBLE,
        LIFE_STAGE_CHANGED,
        APPLICATION_SUBMITTED,
        APPLICATION_STATUS_CHANGED,
        INTERVIEW_SCHEDULED,
        INTERVIEW_COMPLETED,
        OFFER_MADE,
        OFFER_ACCEPTED,
        OFFER_REJECTED,
        PLACEMENT_CREATED,
        DEPLOYED,
        DEACTIVATED,
        BLACKLISTED
    }
}
