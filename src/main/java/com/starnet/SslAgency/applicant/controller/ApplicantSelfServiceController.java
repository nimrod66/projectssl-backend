package com.starnet.SslAgency.applicant.controller;

import com.starnet.SslAgency.applicant.dto.ApplicantConsentDto;
import com.starnet.SslAgency.applicant.dto.ApplicantProfileDto;
import com.starnet.SslAgency.applicant.dto.ApplicantResponseDto;
import com.starnet.SslAgency.applicant.model.Applicant;
import com.starnet.SslAgency.applicant.model.ApplicantConsent;
import com.starnet.SslAgency.applicant.service.ApplicantService;
import com.starnet.SslAgency.applicant.service.ConsentService;
import com.starnet.SslAgency.applicant.service.ReadinessService;
import com.starnet.SslAgency.document.dto.ApplicantDocumentDto;
import com.starnet.SslAgency.document.dto.DocumentRequirementDto;
import com.starnet.SslAgency.document.service.DocumentService;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.recruitment.dto.ApplicationResponseDto;
import com.starnet.SslAgency.recruitment.dto.OfferResponseDto;
import com.starnet.SslAgency.recruitment.model.Application;
import com.starnet.SslAgency.recruitment.model.Offer;
import com.starnet.SslAgency.recruitment.service.ApplicationService;
import com.starnet.SslAgency.recruitment.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applicants/me")
public class ApplicantSelfServiceController {

    @Autowired
    private ApplicantService applicantService;

    @Autowired
    private ConsentService consentService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private OfferService offerService;

    @Autowired
    private ReadinessService readinessService;

    @GetMapping
    @PreAuthorize("hasRole('APPLICANT')")
    public ApplicantResponseDto me(@AuthenticationPrincipal Applicant applicant) {
        return applicantService.get(applicant.getId());
    }

    @PatchMapping("/profile")
    @PreAuthorize("hasRole('APPLICANT')")
    public ApplicantResponseDto updateProfile(@AuthenticationPrincipal Applicant applicant,
                                              @RequestBody @Valid ApplicantProfileDto dto) {
        return applicantService.updateProfile(applicant.getId(), dto);
    }

    @PostMapping("/consent")
    @PreAuthorize("hasRole('APPLICANT')")
    public ApplicantConsentDto grantConsent(@AuthenticationPrincipal Applicant applicant,
                                            @RequestBody Map<String, String> body) {
        String rawType = body.get("consentType");
        if (rawType == null || rawType.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "consentType is required");
        }
        ApplicantConsent.ConsentType type = ApplicantConsent.ConsentType.valueOf(rawType);
        return consentService.grant(applicant.getId(), type, body.get("termsVersion"),
                ApplicantConsent.Source.WEBSITE, null);
    }

    @GetMapping("/consent")
    @PreAuthorize("hasRole('APPLICANT')")
    public List<ApplicantConsentDto> listConsent(@AuthenticationPrincipal Applicant applicant) {
        return consentService.list(applicant.getId());
    }

    @GetMapping("/document-requirements")
    @PreAuthorize("hasRole('APPLICANT')")
    public List<DocumentRequirementDto> requirements(@AuthenticationPrincipal Applicant applicant) {
        return documentService.listRequirements(applicant.getApplicantType(), null);
    }

    @GetMapping("/documents")
    @PreAuthorize("hasRole('APPLICANT')")
    public List<ApplicantDocumentDto> documents(@AuthenticationPrincipal Applicant applicant) {
        return documentService.listCurrentApplicantDocuments(applicant.getId());
    }

    @PostMapping(value = "/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('APPLICANT')")
    public ApplicantDocumentDto uploadDocument(@AuthenticationPrincipal Applicant applicant,
                                               @RequestParam("documentTypeId") Long documentTypeId,
                                               @RequestParam("file") MultipartFile file) {
        return documentService.uploadApplicantDocument(applicant.getId(), documentTypeId, file, null);
    }

    @PostMapping("/applications")
    @PreAuthorize("hasRole('APPLICANT')")
    public ApplicationResponseDto apply(@AuthenticationPrincipal Applicant applicant,
                                        @RequestBody Map<String, Long> body) {
        if (body.get("opportunityId") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "opportunityId is required");
        }
        return applicationService.apply(applicant.getId(), body.get("opportunityId"), null);
    }

    @GetMapping("/applications")
    @PreAuthorize("hasRole('APPLICANT')")
    public List<ApplicationResponseDto> applications(@AuthenticationPrincipal Applicant applicant) {
        return applicationService.listByApplicant(applicant.getId());
    }

    @PostMapping("/applications/{id}/withdraw")
    @PreAuthorize("hasRole('APPLICANT')")
    public ApplicationResponseDto withdraw(@AuthenticationPrincipal Applicant applicant,
                                           @PathVariable Long id,
                                           @RequestBody(required = false) Map<String, String> body) {
        requireApplicationOwner(applicant, id);
        return applicationService.withdraw(id, body != null ? body.get("reason") : null);
    }

    @GetMapping("/applications/{id}/offers")
    @PreAuthorize("hasRole('APPLICANT')")
    public List<OfferResponseDto> offers(@AuthenticationPrincipal Applicant applicant, @PathVariable Long id) {
        requireApplicationOwner(applicant, id);
        return offerService.listByApplication(id);
    }

    @PatchMapping("/offers/{id}/accept")
    @PreAuthorize("hasRole('APPLICANT')")
    public OfferResponseDto acceptOffer(@AuthenticationPrincipal Applicant applicant, @PathVariable Long id) {
        requireOfferOwner(applicant, id);
        return offerService.accept(id, null);
    }

    @PatchMapping("/offers/{id}/reject")
    @PreAuthorize("hasRole('APPLICANT')")
    public OfferResponseDto rejectOffer(@AuthenticationPrincipal Applicant applicant,
                                        @PathVariable Long id,
                                        @RequestBody(required = false) Map<String, String> body) {
        requireOfferOwner(applicant, id);
        return offerService.reject(id, body != null ? body.get("reason") : null, null);
    }

    @GetMapping("/readiness")
    @PreAuthorize("hasRole('APPLICANT')")
    public ReadinessService.ReadinessResult readiness(@AuthenticationPrincipal Applicant applicant) {
        return readinessService.applicantReadiness(applicant.getId());
    }

    @GetMapping("/eligibility/{opportunityId}")
    @PreAuthorize("hasRole('APPLICANT')")
    public ReadinessService.ReadinessResult eligibility(@AuthenticationPrincipal Applicant applicant,
                                                        @PathVariable Long opportunityId) {
        return readinessService.opportunityEligibility(applicant.getId(), opportunityId);
    }

    private void requireApplicationOwner(Applicant applicant, Long applicationId) {
        Application application = applicationService.getEntity(applicationId);
        if (!application.getApplicant().getId().equals(applicant.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your application");
        }
    }

    private void requireOfferOwner(Applicant applicant, Long offerId) {
        Offer offer = offerService.getEntity(offerId);
        if (!offer.getApplication().getApplicant().getId().equals(applicant.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your offer");
        }
    }
}
