package com.starnet.SslAgency.interapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterApplicationResponseDto {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private String email;
    private LocalDate dob;
    private Integer age;
    private String nationality;
    private String religion;
    private String maritalStatus;
    private String numberOfKids;
    private String educationLevel;
    private String currentProfession;
    private Double currentSalary;
    private String currentLocation;
    private List<String> languages;
    private String employmentStatus;
    private String jobRecruitment;
    private String status;

    private String createdAt;
    private String updatedAt;

    private Long vettedById;
    private String vettedByName;
    private Long approvedById;
    private String approvedByName;


    private List<String> passportPhotos;
    private List<String> fullPhotos;
    private List<String> nationalIdPhotos;
    private List<String> resumes;
    private List<String> birthCertificates;
    private List<String> goodConducts;
    private List<String> videos;
    private List<String> showcasePhotos;
}
