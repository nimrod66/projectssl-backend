package com.starnet.SslAgency.employer.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployerResponseDto {
    private Long id;
    private String companyName;
    private String country;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String notes;
    private String status;
    private String createdAt;
    private String updatedAt;
}
