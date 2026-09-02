package com.starnet.SslAgency.recruitment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationRequestDto {

    @NotNull
    private Long applicantId;

    @NotNull
    private Long opportunityId;

    private Long assignedRecruiterId;
}
