package com.starnet.SslAgency.placement.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementResponseDto {
    private Long id;
    private Long contractId;
    private String jobCategory;
    private String country;
    private Long applicationId;
    private Long interApplicationId;
    private String candidateType;
    private String candidateName;
    private String stage;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private BigDecimal salary;
    private String currency;
    private String notes;
    private Long assignedBy;
    private String assignedAt;
    private String createdAt;
    private String updatedAt;
    private String employerName;
}
