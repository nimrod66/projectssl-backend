package com.starnet.SslAgency.interapplication.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterApplicationRequestDto {
    @NotBlank
    private String firstName;
    private String middleName;
    @NotBlank
    private String lastName;

    @Pattern(regexp = "^(\\+254|0)(7\\d{8}|1\\d{8})$", message = "Invalid Kenyan phone number")
    private String phoneNumber;

    @NotBlank
    private String email;

    @Past
    private LocalDate dob;

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
}
