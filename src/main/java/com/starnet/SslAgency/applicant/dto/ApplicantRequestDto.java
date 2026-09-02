package com.starnet.SslAgency.applicant.dto;

import com.starnet.SslAgency.applicant.model.Applicant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicantRequestDto {

    @NotBlank
    private String firstName;

    private String middleName;

    @NotBlank
    private String lastName;

    private String email;

    @NotBlank
    @Pattern(regexp = "^(\\+254|0)(7\\d{8}|1\\d{8})$", message = "Invalid Kenyan phone number")
    private String phoneNumber;

    private String alternativePhone;

    private LocalDate dateOfBirth;

    private Applicant.Gender gender;

    private String nationality;

    private String county;

    private String address;

    private Applicant.RegistrationSource registrationSource;

    private Applicant.ApplicantType applicantType;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one number"
    )
    private String password;
}
