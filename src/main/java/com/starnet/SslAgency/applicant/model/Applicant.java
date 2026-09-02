package com.starnet.SslAgency.applicant.model;

import com.starnet.SslAgency.processor.model.Staff;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "applicants", indexes = {
        @Index(name = "idx_applicant_phone", columnList = "phone_number"),
        @Index(name = "idx_applicant_number", columnList = "applicant_number"),
        @Index(name = "idx_applicant_stage", columnList = "lifecycle_stage")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Applicant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "applicant_number", nullable = false, unique = true, length = 32)
    private String applicantNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicant_type", nullable = false, length = 20)
    @Builder.Default
    private ApplicantType applicantType = ApplicantType.LOCAL;

    @NotBlank
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @NotBlank
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Email
    private String email;

    @NotBlank
    @Column(name = "phone_number", nullable = false, unique = true, length = 32)
    private String phoneNumber;

    @Column(name = "alternative_phone", length = 32)
    private String alternativePhone;

    @Past
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    private String nationality;
    private String county;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_source", nullable = false, length = 30)
    @Builder.Default
    private RegistrationSource registrationSource = RegistrationSource.WEBSITE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_recruiter_id")
    private Staff assignedRecruiter;

    @Enumerated(EnumType.STRING)
    @Column(name = "lifecycle_stage", nullable = false, length = 30)
    @Builder.Default
    private LifecycleStage lifecycleStage = LifecycleStage.REGISTERED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @JsonIgnore
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private ApplicantProfile profile;

    public void attachProfile(ApplicantProfile profile) {
        this.profile = profile;
        if (profile != null && profile.getApplicant() != this) {
            profile.setApplicant(this);
        }
    }

    public enum ApplicantType {
        LOCAL, INTERNATIONAL
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public enum RegistrationSource {
        WEBSITE, WALK_IN, RECRUITMENT_DRIVE, REFERRAL, PARTNER, SOCIAL_MEDIA, STAFF_ENTRY, OTHER
    }

    public enum LifecycleStage {
        REGISTERED, PROFILE_COMPLETE, UNDER_REVIEW, VETTED, ELIGIBLE, INACTIVE, BLACKLISTED
    }

    public enum Status {
        ACTIVE, INACTIVE, BLACKLISTED
    }
}
