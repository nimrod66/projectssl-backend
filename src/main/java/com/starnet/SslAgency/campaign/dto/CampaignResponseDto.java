package com.starnet.SslAgency.campaign.dto;

import com.starnet.SslAgency.campaign.model.Campaign;
import com.starnet.SslAgency.applicant.model.Applicant;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignResponseDto {

    private Long id;
    private String name;
    private String description;
    private Applicant.ApplicantType targetApplicantType;
    private Campaign.Status status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CampaignMemberDto> members;

    public static CampaignResponseDto from(Campaign c, List<CampaignMemberDto> members) {
        return CampaignResponseDto.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .targetApplicantType(c.getTargetApplicantType())
                .status(c.getStatus())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .createdById(c.getCreatedBy() != null ? c.getCreatedBy().getId() : null)
                .createdByName(c.getCreatedBy() != null
                        ? c.getCreatedBy().getFirstName() + " " + c.getCreatedBy().getLastName() : null)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .members(members)
                .build();
    }
}