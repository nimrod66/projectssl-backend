package com.starnet.SslAgency.contract.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractResponseDto {
    private Long id;
    private Long employerId;
    private String employerName;
    private String jobCategory;
    private String country;
    private int numberOfPositions;
    private int filledPositions;
    private BigDecimal salary;
    private String currency;
    private int durationMonths;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean renewable;
    private String notes;
    private String status;
    private Long createdBy;
    private String createdAt;
    private String updatedAt;
}
