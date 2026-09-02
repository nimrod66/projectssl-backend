package com.starnet.SslAgency.opportunity.dto;

import com.starnet.SslAgency.opportunity.model.Opportunity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpportunityResponseDto {

    private Long id;
    private String title;
    private String description;
    private String country;
    private String location;
    private String jobCategory;
    private Integer numberOfPositions;
    private Integer filledPositions;
    private BigDecimal salaryMinimum;
    private BigDecimal salaryMaximum;
    private String currency;
    private Integer durationMonths;
    private LocalDate startDate;
    private String benefits;
    private String termsAndConditions;
    private String workingHours;
    private Boolean accommodationProvided;
    private Boolean transportProvided;
    private String requiredExperience;
    private String requiredEducation;
    private String requiredSkills;
    private String requiredLanguages;
    private Integer minimumAge;
    private Integer maximumAge;
    private Opportunity.RequiredGender genderRequirement;
    private LocalDate applicationDeadline;
    private Opportunity.Status status;
    private Long employerId;
    private String employerName;
    private Long contractId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OpportunityResponseDto from(Opportunity o) {
        return OpportunityResponseDto.builder()
                .id(o.getId())
                .title(o.getTitle())
                .description(o.getDescription())
                .country(o.getCountry())
                .location(o.getLocation())
                .jobCategory(o.getJobCategory())
                .numberOfPositions(o.getNumberOfPositions())
                .filledPositions(o.getFilledPositions())
                .salaryMinimum(o.getSalaryMinimum())
                .salaryMaximum(o.getSalaryMaximum())
                .currency(o.getCurrency())
                .durationMonths(o.getDurationMonths())
                .startDate(o.getStartDate())
                .benefits(o.getBenefits())
                .termsAndConditions(o.getTermsAndConditions())
                .workingHours(o.getWorkingHours())
                .accommodationProvided(o.isAccommodationProvided())
                .transportProvided(o.isTransportProvided())
                .requiredExperience(o.getRequiredExperience())
                .requiredEducation(o.getRequiredEducation())
                .requiredSkills(o.getRequiredSkills())
                .requiredLanguages(o.getRequiredLanguages())
                .minimumAge(o.getMinimumAge())
                .maximumAge(o.getMaximumAge())
                .genderRequirement(o.getGenderRequirement())
                .applicationDeadline(o.getApplicationDeadline())
                .status(o.getStatus())
                .employerId(o.getEmployer() != null ? o.getEmployer().getId() : null)
                .employerName(o.getEmployer() != null ? o.getEmployer().getCompanyName() : null)
                .contractId(o.getContract() != null ? o.getContract().getId() : null)
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }
}
