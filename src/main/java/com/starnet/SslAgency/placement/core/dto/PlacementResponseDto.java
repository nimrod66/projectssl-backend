package com.starnet.SslAgency.placement.core.dto;

import com.starnet.SslAgency.placement.core.model.Placement;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementResponseDto {

    private Long id;
    private String placementNumber;
    private Long applicantId;
    private String applicantName;
    private String applicantNumber;
    private Long applicationId;
    private Long acceptedOfferId;
    private Long opportunityId;
    private String opportunityTitle;
    private Long employerId;
    private String employerName;
    private Long contractId;
    private Placement.Stage stage;
    private boolean active;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private String terminationReason;
    private String returnReason;
    private LocalDateTime createdAt;
    private List<PlacementStatusHistoryDto> history;
    private List<PlacementChecklistDto> checklist;

    public static PlacementResponseDto from(Placement p, List<PlacementStatusHistoryDto> history,
                                           List<PlacementChecklistDto> checklist) {
        return PlacementResponseDto.builder()
                .id(p.getId())
                .placementNumber(p.getPlacementNumber())
                .applicantId(p.getApplicant().getId())
                .applicantName(p.getApplicant().getFirstName() + " " + p.getApplicant().getLastName())
                .applicantNumber(p.getApplicant().getApplicantNumber())
                .applicationId(p.getApplication().getId())
                .acceptedOfferId(p.getAcceptedOffer().getId())
                .opportunityId(p.getOpportunity().getId())
                .opportunityTitle(p.getOpportunity().getTitle())
                .employerId(p.getEmployer().getId())
                .employerName(p.getEmployer().getCompanyName())
                .contractId(p.getContract().getId())
                .stage(p.getStage())
                .active(p.isActive())
                .startDate(p.getStartDate())
                .expectedEndDate(p.getExpectedEndDate())
                .terminationReason(p.getTerminationReason())
                .returnReason(p.getReturnReason())
                .createdAt(p.getCreatedAt())
                .history(history)
                .checklist(checklist)
                .build();
    }
}