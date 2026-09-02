package com.starnet.SslAgency.applicant.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "applicant_consents", indexes = {
        @Index(name = "idx_consent_applicant", columnList = "applicant_id"),
        @Index(name = "idx_consent_active", columnList = "applicant_id,status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicantConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    @ToString.Exclude
    private Applicant applicant;

    @Enumerated(EnumType.STRING)
    @Column(name = "consent_type", nullable = false, length = 30)
    private ConsentType consentType;

    @Column(name = "terms_version")
    private String termsVersion;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column(name = "signed_at", nullable = false)
    @Builder.Default
    private LocalDateTime signedAt = LocalDateTime.now();

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Source source;

    @Column(name = "storage_url")
    private String storageUrl;

    public enum ConsentType {
        DATA_PROCESSING, MEDICAL, TRAVEL, CONTACT, MARKETING, TERMS_AND_CONDITIONS
    }

    public enum Status {
        ACTIVE, REVOKED
    }

    public enum Source {
        WEBSITE, OFFICE, RECRUITMENT_DRIVE, PHONE
    }
}