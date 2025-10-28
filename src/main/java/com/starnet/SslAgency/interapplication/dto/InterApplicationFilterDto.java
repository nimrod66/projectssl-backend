package com.starnet.SslAgency.interapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data

public class InterApplicationFilterDto
{

    private String nationality;

    private String jobRecruitment;

}