package com.starnet.SslAgency.document.dto;

import com.starnet.SslAgency.applicant.model.Applicant;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRequirementRequestDto {

    private Long documentTypeId;
    private String documentTypeCode;
    private Applicant.ApplicantType applicantType;
    private Long opportunityId;
    private boolean required = true;
}