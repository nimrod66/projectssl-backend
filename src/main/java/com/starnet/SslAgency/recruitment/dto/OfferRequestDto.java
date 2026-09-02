package com.starnet.SslAgency.recruitment.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferRequestDto {

    private Long applicationId;

    private BigDecimal offeredSalary;

    private String currency;

    private String positionTitle;

    private LocalDate startDate;

    private String benefits;

    private String conditions;

    private LocalDate expiresAt;
}
