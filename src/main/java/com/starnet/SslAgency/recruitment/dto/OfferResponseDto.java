package com.starnet.SslAgency.recruitment.dto;

import com.starnet.SslAgency.recruitment.model.Offer;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferResponseDto {

    private Long id;
    private Long applicationId;
    private Long applicantId;
    private String applicantName;
    private String opportunityTitle;
    private BigDecimal offeredSalary;
    private String currency;
    private String positionTitle;
    private LocalDate startDate;
    private String benefits;
    private String conditions;
    private Offer.Status status;
    private String rejectionReason;
    private LocalDateTime offeredAt;
    private LocalDateTime respondedAt;
    private LocalDateTime expiresAt;

    public static OfferResponseDto from(Offer o) {
        return OfferResponseDto.builder()
                .id(o.getId())
                .applicationId(o.getApplication().getId())
                .applicantId(o.getApplication().getApplicant().getId())
                .applicantName(o.getApplication().getApplicant().getFirstName() + " "
                        + o.getApplication().getApplicant().getLastName())
                .opportunityTitle(o.getApplication().getOpportunity().getTitle())
                .offeredSalary(o.getOfferedSalary())
                .currency(o.getCurrency())
                .positionTitle(o.getPositionTitle())
                .startDate(o.getStartDate())
                .benefits(o.getBenefits())
                .conditions(o.getConditions())
                .status(o.getStatus())
                .rejectionReason(o.getRejectionReason())
                .offeredAt(o.getOfferedAt())
                .respondedAt(o.getRespondedAt())
                .expiresAt(o.getExpiresAt())
                .build();
    }
}
