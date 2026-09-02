package com.starnet.SslAgency.campaign.dto;

import com.starnet.SslAgency.campaign.model.CampaignMember;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignMemberDto {

    private Long id;
    private Long applicantId;
    private String applicantName;
    private String applicantNumber;
    private LocalDateTime addedAt;

    public static CampaignMemberDto from(CampaignMember m) {
        return CampaignMemberDto.builder()
                .id(m.getId())
                .applicantId(m.getApplicant().getId())
                .applicantName(m.getApplicant().getFirstName() + " " + m.getApplicant().getLastName())
                .applicantNumber(m.getApplicant().getApplicantNumber())
                .addedAt(m.getAddedAt())
                .build();
    }
}