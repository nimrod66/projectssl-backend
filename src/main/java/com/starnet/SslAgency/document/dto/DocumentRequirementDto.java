package com.starnet.SslAgency.document.dto;

import com.starnet.SslAgency.applicant.model.Applicant;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRequirementDto {

    private Long id;
    private DocumentTypeDto documentType;
    private Applicant.ApplicantType applicantType;
    private Long opportunityId;
    private boolean required;

    public static DocumentRequirementDto from(com.starnet.SslAgency.document.model.DocumentRequirement r) {
        return DocumentRequirementDto.builder()
                .id(r.getId())
                .documentType(DocumentTypeDto.from(r.getDocumentType()))
                .applicantType(r.getApplicantType())
                .opportunityId(r.getOpportunity() != null ? r.getOpportunity().getId() : null)
                .required(r.isRequired())
                .build();
    }
}