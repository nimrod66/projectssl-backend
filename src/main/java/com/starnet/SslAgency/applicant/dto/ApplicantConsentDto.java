package com.starnet.SslAgency.applicant.dto;

import com.starnet.SslAgency.applicant.model.ApplicantConsent;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicantConsentDto {

    private Long id;
    private Long applicantId;
    private ApplicantConsent.ConsentType consentType;
    private String termsVersion;
    private ApplicantConsent.Status status;
    private LocalDateTime signedAt;
    private LocalDateTime revokedAt;
    private ApplicantConsent.Source source;
    private String storageUrl;

    public static ApplicantConsentDto from(ApplicantConsent c) {
        return ApplicantConsentDto.builder()
                .id(c.getId())
                .applicantId(c.getApplicant().getId())
                .consentType(c.getConsentType())
                .termsVersion(c.getTermsVersion())
                .status(c.getStatus())
                .signedAt(c.getSignedAt())
                .revokedAt(c.getRevokedAt())
                .source(c.getSource())
                .storageUrl(c.getStorageUrl())
                .build();
    }
}