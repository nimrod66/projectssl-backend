package com.starnet.SslAgency.document.model;

import com.starnet.SslAgency.applicant.model.Applicant;
import com.starnet.SslAgency.processor.model.Staff;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "applicant_documents", indexes = {
        @Index(name = "idx_appdoc_applicant", columnList = "applicant_id"),
        @Index(name = "idx_appdoc_current", columnList = "applicant_id,is_current")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicantDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    @ToString.Exclude
    private Applicant applicant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_type_id", nullable = false)
    @ToString.Exclude
    private DocumentType documentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_asset_id")
    @ToString.Exclude
    private FileAsset fileAsset;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    @Builder.Default
    private Status status = Status.UPLOADED;

    @Column(nullable = false)
    @Builder.Default
    private int version = 1;

    @Column(name = "is_current", nullable = false)
    @Builder.Default
    private boolean current = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    @ToString.Exclude
    private Staff verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();

    public enum Status {
        NOT_SUBMITTED, UPLOADED, UNDER_REVIEW, VERIFIED, REJECTED, RESUBMISSION_REQUIRED, EXPIRED
    }
}