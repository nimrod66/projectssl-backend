package com.starnet.SslAgency.opportunity.dto;

import com.starnet.SslAgency.opportunity.model.Opportunity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpportunityRequestDto {

    @NotNull
    private Long employerId;

    private Long contractId;

    @NotBlank
    private String title;

    private String description;

    private String country;

    private String location;

    private String jobCategory;

    @Positive
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
}
