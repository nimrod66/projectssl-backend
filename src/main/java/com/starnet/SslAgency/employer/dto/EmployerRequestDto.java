package com.starnet.SslAgency.employer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployerRequestDto {
    @NotBlank
    private String companyName;
    private String country;
    private String contactName;
    @Email
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String notes;
    private String status;
}
