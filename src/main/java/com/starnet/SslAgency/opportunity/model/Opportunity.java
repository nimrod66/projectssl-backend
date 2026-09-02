package com.starnet.SslAgency.opportunity.model;

import com.starnet.SslAgency.contract.model.Contract;
import com.starnet.SslAgency.employer.model.Employer;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.recruitment.model.Application;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "opportunities", indexes = {
        @Index(name = "idx_opportunity_status", columnList = "status"),
        @Index(name = "idx_opportunity_country", columnList = "country"),
        @Index(name = "idx_opportunity_employer", columnList = "employer_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employer_id", nullable = false)
    private Employer employer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank
    @Column(nullable = false)
    private String country;

    private String location;
    private String jobCategory;

    @Column(nullable = false)
    private int numberOfPositions;

    @Builder.Default
    private int filledPositions = 0;

    private BigDecimal salaryMinimum;
    private BigDecimal salaryMaximum;
    private String currency;
    private Integer durationMonths;
    private LocalDate startDate;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @Column(columnDefinition = "TEXT")
    private String termsAndConditions;

    private String workingHours;
    private boolean accommodationProvided;
    private boolean transportProvided;
    private String requiredExperience;
    private String requiredEducation;

    @Column(columnDefinition = "TEXT")
    private String requiredSkills;

    @Column(columnDefinition = "TEXT")
    private String requiredLanguages;

    private Integer minimumAge;
    private Integer maximumAge;

    @Enumerated(EnumType.STRING)
    private RequiredGender genderRequirement;

    private LocalDate applicationDeadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    @Builder.Default
    private Status status = Status.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Staff createdBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "opportunity")
    @Builder.Default
    @ToString.Exclude
    private List<Application> applications = new ArrayList<>();

    public enum Status {
        DRAFT, PENDING_APPROVAL, OPEN, PAUSED, FILLED, CLOSED
    }

    public enum RequiredGender {
        MALE, FEMALE, OTHER
    }
}
