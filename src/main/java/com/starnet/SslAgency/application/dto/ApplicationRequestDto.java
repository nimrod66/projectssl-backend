package com.starnet.SslAgency.application.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequestDto {
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

    private String experience;

    private Double currentSalary;

    private String currentProfession;

    private Boolean hasCat;
    private Boolean hasDog;
    private Boolean extraPay;
    private Boolean liveOut;
    private Boolean privateRoom;
    private Boolean elderlyCare;
    private Boolean specialNeeds;
    private Boolean olderThan1;
    private Boolean youngerThan1;

    private String currentLocation;

    private List<String> languages;
    private String employmentStatus;
    private String jobInterest;


}
