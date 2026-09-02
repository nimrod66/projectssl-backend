package com.starnet.SslAgency.document.dto;

import com.starnet.SslAgency.document.model.ApplicantDocument;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicantDocumentDto {

    private Long id;
    private Long applicantId;
    private DocumentTypeDto documentType;
    private String fileUrl;
    private String originalName;
    private ApplicantDocument.Status status;
    private int version;
    private boolean current;
    private String verifiedByName;
    private LocalDateTime verifiedAt;
    private String rejectionReason;
    private LocalDateTime uploadedAt;

    public static ApplicantDocumentDto from(ApplicantDocument d) {
        return ApplicantDocumentDto.builder()
                .id(d.getId())
                .applicantId(d.getApplicant().getId())
                .documentType(DocumentTypeDto.from(d.getDocumentType()))
                .fileUrl(d.getFileAsset() != null ? d.getFileAsset().getFileUrl() : null)
                .originalName(d.getFileAsset() != null ? d.getFileAsset().getOriginalName() : null)
                .status(d.getStatus())
                .version(d.getVersion())
                .current(d.isCurrent())
                .verifiedByName(d.getVerifiedBy() != null
                        ? d.getVerifiedBy().getFirstName() + " " + d.getVerifiedBy().getLastName() : null)
                .verifiedAt(d.getVerifiedAt())
                .rejectionReason(d.getRejectionReason())
                .uploadedAt(d.getUploadedAt())
                .build();
    }
}