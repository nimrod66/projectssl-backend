package com.starnet.SslAgency.applicant.dto;

import com.starnet.SslAgency.applicant.model.ApplicantProfile;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicantProfileDto {

    private String educationLevel;
    private String fieldOfStudy;
    private String professionalSummary;
    private Integer yearsOfExperience;
    private String skills;
    private String languages;
    private String preferredJobCategories;
    private String preferredCountries;
    private BigDecimal preferredSalary;
    private String preferredSalaryCurrency;
    private ApplicantProfile.Availability availability;
    private LocalDate availableFrom;
    private Boolean willingToRelocate;
    private String employmentStatus;
    private String currentEmployer;
    private String currentPosition;
    private String relevantExperience;
    private String reasonForLeaving;
    private String religion;
    private ApplicantProfile.MaritalStatus maritalStatus;
    private Integer numberOfChildren;
    private String nextOfKinName;
    private String nextOfKinPhone;
    private String nextOfKinRelationship;
}
