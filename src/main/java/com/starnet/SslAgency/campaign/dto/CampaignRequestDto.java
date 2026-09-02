package com.starnet.SslAgency.campaign.dto;

import com.starnet.SslAgency.campaign.model.Campaign;
import com.starnet.SslAgency.applicant.model.Applicant;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignRequestDto {

    @NotBlank
    private String name;

    private String description;

    private Applicant.ApplicantType targetApplicantType;

    private LocalDate startDate;

    private LocalDate endDate;
}