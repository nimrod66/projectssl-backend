package com.starnet.SslAgency.applicant.service;

import com.starnet.SslAgency.applicant.dto.ApplicantConsentDto;
import com.starnet.SslAgency.applicant.model.Applicant;
import com.starnet.SslAgency.applicant.model.ApplicantConsent;
import com.starnet.SslAgency.applicant.model.ApplicantTimeline;
import com.starnet.SslAgency.applicant.repository.ApplicantConsentRepository;
import com.starnet.SslAgency.processor.model.Staff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsentService {

    @Autowired
    private ApplicantConsentRepository consentRepository;

    @Autowired
    private ApplicantService applicantService;

    @Autowired
    private ApplicantTimelineService timelineService;

    @Transactional
    public ApplicantConsentDto grant(Long applicantId, ApplicantConsent.ConsentType consentType,
                                     String termsVersion, ApplicantConsent.Source source, Staff actor) {
        Applicant applicant = applicantService.getEntity(applicantId);

        ApplicantConsent consent = ApplicantConsent.builder()
                .applicant(applicant)
                .consentType(consentType)
                .termsVersion(termsVersion)
                .status(ApplicantConsent.Status.ACTIVE)
                .source(source != null ? source : ApplicantConsent.Source.OFFICE)
                .build();
        consent = consentRepository.save(consent);

        timelineService.log(applicant, ApplicantTimeline.EventType.PROFILE_UPDATED,
                "Consent granted: " + consentType.name(), actor, "consentId=" + consent.getId());

        return ApplicantConsentDto.from(consent);
    }

    @Transactional
    public ApplicantConsentDto revoke(Long consentId, Staff actor) {
        ApplicantConsent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consent not found"));
        if (consent.getStatus() == ApplicantConsent.Status.REVOKED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Consent already revoked");
        }
        consent.setStatus(ApplicantConsent.Status.REVOKED);
        consent.setRevokedAt(LocalDateTime.now());
        consent = consentRepository.save(consent);

        timelineService.log(consent.getApplicant(), ApplicantTimeline.EventType.PROFILE_UPDATED,
                "Consent revoked: " + consent.getConsentType().name(), actor,
                "consentId=" + consent.getId());

        return ApplicantConsentDto.from(consent);
    }

    public List<ApplicantConsentDto> list(Long applicantId) {
        return consentRepository.findByApplicantIdOrderBySignedAtDesc(applicantId).stream()
                .map(ApplicantConsentDto::from)
                .toList();
    }

    public boolean hasActiveConsent(Long applicantId) {
        return consentRepository.existsByApplicantIdAndStatus(applicantId, ApplicantConsent.Status.ACTIVE);
    }
}