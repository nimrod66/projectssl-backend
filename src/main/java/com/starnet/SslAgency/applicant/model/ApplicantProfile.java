package com.starnet.SslAgency.applicant.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "applicant_profiles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicantProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false, unique = true)
    @ToString.Exclude
    private Applicant applicant;

    private String educationLevel;
    private String fieldOfStudy;

    @Column(columnDefinition = "TEXT")
    private String professionalSummary;

    private Integer yearsOfExperience;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String languages;

    @Column(columnDefinition = "TEXT")
    private String preferredJobCategories;

    @Column(columnDefinition = "TEXT")
    private String preferredCountries;

    private BigDecimal preferredSalary;
    private String preferredSalaryCurrency;

    @Enumerated(EnumType.STRING)
    private Availability availability;

    private LocalDate availableFrom;

    @Builder.Default
    private boolean willingToRelocate = false;

    private String employmentStatus;
    private String currentEmployer;
    private String currentPosition;

    @Column(columnDefinition = "TEXT")
    private String relevantExperience;

    @Column(columnDefinition = "TEXT")
    private String reasonForLeaving;

    // These fields are optional and relevant only where international
    // recruitment legitimately requires them.
    private String religion;

    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;

    private Integer numberOfChildren;
    private String nextOfKinName;
    private String nextOfKinPhone;
    private String nextOfKinRelationship;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Availability {
        AVAILABLE_IMMEDIATELY, AVAILABLE_IN_30_DAYS, EMPLOYED, UNAVAILABLE
    }

    public enum MaritalStatus {
        SINGLE, MARRIED, DIVORCED, WIDOWED
    }
}
