package com.starnet.SslAgency.interapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterApplicationCVDto {
    private Long id;
    private String fullName;
    private String nationality;

    private String jobRecruitment;
    private String religion;

    private String currentProfession;
    private Double currentSalary;

    private LocalDate dob;
    private Integer age;

    private String maritalStatus;
    private String numberOfKids;

    private String educationLevel;

    private List<String> languages;
    private String employmentStatus;


}
