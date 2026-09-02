package com.starnet.SslAgency.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractRequestDto {
    @NotNull
    private Long employerId;
    @NotBlank
    private String jobCategory;
    @NotBlank
    private String country;
    @Positive
    private int numberOfPositions;
    @NotNull
    @Positive
    private BigDecimal salary;
    private String currency;
    private int durationMonths;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean renewable;
    private String notes;
}
