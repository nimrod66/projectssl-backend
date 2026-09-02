package com.starnet.SslAgency.applicant.dto;

import com.starnet.SslAgency.applicant.model.Applicant;
import com.starnet.SslAgency.applicant.model.ApplicantProfile;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicantResponseDto {

    private Long id;
    private String applicantNumber;
    private Applicant.ApplicantType applicantType;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String alternativePhone;
    private LocalDate dateOfBirth;
    private Applicant.Gender gender;
    private String nationality;
    private String county;
    private String address;
    private Applicant.RegistrationSource registrationSource;
    private Applicant.LifecycleStage lifecycleStage;
    private Applicant.Status status;
    private Long assignedRecruiterId;
    private String assignedRecruiterName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ApplicantProfileDto profile;

    public static ApplicantResponseDto from(Applicant a) {
        ApplicantProfile p = a.getProfile();
        ApplicantProfileDto profileDto = null;
        if (p != null) {
            profileDto = ApplicantProfileDto.builder()
                    .educationLevel(p.getEducationLevel())
                    .fieldOfStudy(p.getFieldOfStudy())
                    .professionalSummary(p.getProfessionalSummary())
                    .yearsOfExperience(p.getYearsOfExperience())
                    .skills(p.getSkills())
                    .languages(p.getLanguages())
                    .preferredJobCategories(p.getPreferredJobCategories())
                    .preferredCountries(p.getPreferredCountries())
                    .preferredSalary(p.getPreferredSalary())
                    .preferredSalaryCurrency(p.getPreferredSalaryCurrency())
                    .availability(p.getAvailability())
                    .availableFrom(p.getAvailableFrom())
                    .willingToRelocate(p.isWillingToRelocate())
                    .employmentStatus(p.getEmploymentStatus())
                    .currentEmployer(p.getCurrentEmployer())
                    .currentPosition(p.getCurrentPosition())
                    .relevantExperience(p.getRelevantExperience())
                    .reasonForLeaving(p.getReasonForLeaving())
                    .religion(p.getReligion())
                    .maritalStatus(p.getMaritalStatus())
                    .numberOfChildren(p.getNumberOfChildren())
                    .nextOfKinName(p.getNextOfKinName())
                    .nextOfKinPhone(p.getNextOfKinPhone())
                    .nextOfKinRelationship(p.getNextOfKinRelationship())
                    .build();
        }

        String fullName = String.join(" ",
                java.util.stream.Stream.of(a.getFirstName(), a.getMiddleName(), a.getLastName())
                        .filter(s -> s != null && !s.isBlank())
                        .toList());

        return ApplicantResponseDto.builder()
                .id(a.getId())
                .applicantNumber(a.getApplicantNumber())
                .applicantType(a.getApplicantType())
                .firstName(a.getFirstName())
                .middleName(a.getMiddleName())
                .lastName(a.getLastName())
                .fullName(fullName)
                .email(a.getEmail())
                .phoneNumber(a.getPhoneNumber())
                .alternativePhone(a.getAlternativePhone())
                .dateOfBirth(a.getDateOfBirth())
                .gender(a.getGender())
                .nationality(a.getNationality())
                .county(a.getCounty())
                .address(a.getAddress())
                .registrationSource(a.getRegistrationSource())
                .lifecycleStage(a.getLifecycleStage())
                .status(a.getStatus())
                .assignedRecruiterId(a.getAssignedRecruiter() != null ? a.getAssignedRecruiter().getId() : null)
                .assignedRecruiterName(a.getAssignedRecruiter() != null
                        ? a.getAssignedRecruiter().getFirstName() + " " + a.getAssignedRecruiter().getLastName()
                        : null)
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .profile(profileDto)
                .build();
    }
}
