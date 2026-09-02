package com.starnet.SslAgency.applicant.service;

import com.starnet.SslAgency.applicant.model.Applicant;
import com.starnet.SslAgency.applicant.model.ApplicantConsent;
import com.starnet.SslAgency.applicant.model.ApplicantProfile;
import com.starnet.SslAgency.applicant.repository.ApplicantConsentRepository;
import com.starnet.SslAgency.document.model.ApplicantDocument;
import com.starnet.SslAgency.document.repository.ApplicantDocumentRepository;
import com.starnet.SslAgency.document.model.DocumentRequirement;
import com.starnet.SslAgency.document.repository.DocumentRequirementRepository;
import com.starnet.SslAgency.opportunity.model.Opportunity;
import com.starnet.SslAgency.opportunity.service.OpportunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ReadinessService {

    private static final Set<Applicant.LifecycleStage> ELIGIBLE_STAGES = Set.of(
            Applicant.LifecycleStage.PROFILE_COMPLETE, Applicant.LifecycleStage.UNDER_REVIEW,
            Applicant.LifecycleStage.VETTED, Applicant.LifecycleStage.ELIGIBLE);

    private static final Set<ApplicantDocument.Status> SUBMITTED_DOC_STATUSES = Set.of(
            ApplicantDocument.Status.UPLOADED, ApplicantDocument.Status.UNDER_REVIEW,
            ApplicantDocument.Status.VERIFIED);

    @Autowired
    private ApplicantService applicantService;

    @Autowired
    private OpportunityService opportunityService;

    @Autowired
    private ApplicantConsentRepository consentRepository;

    @Autowired
    private ApplicantDocumentRepository applicantDocumentRepository;

    @Autowired
    private DocumentRequirementRepository requirementRepository;

    public record ReadinessResult(boolean ready, List<String> explanations) {}

    public ReadinessResult applicantReadiness(Long applicantId) {
        Applicant applicant = applicantService.getEntity(applicantId);
        List<String> failures = new ArrayList<>();

        if (applicant.getStatus() != Applicant.Status.ACTIVE) {
            failures.add("Applicant is not active (status: " + applicant.getStatus() + ")");
        }
        if (!ELIGIBLE_STAGES.contains(applicant.getLifecycleStage())) {
            failures.add("Applicant lifecycle stage is " + applicant.getLifecycleStage()
                    + "; expected PROFILE_COMPLETE or beyond");
        }
        if (applicant.getAssignedRecruiter() == null) {
            failures.add("No recruiter assigned");
        }

        ApplicantProfile profile = applicant.getProfile();
        if (profile == null || profile.getEducationLevel() == null
                || profile.getYearsOfExperience() == null || profile.getAvailability() == null) {
            failures.add("Applicant profile is incomplete");
        }

        if (!consentRepository.existsByApplicantIdAndStatus(applicantId, ApplicantConsent.Status.ACTIVE)) {
            failures.add("No active consent on record");
        }

        checkRequiredDocuments(applicant, null, failures);

        return new ReadinessResult(failures.isEmpty(), failures);
    }

    public List<String> preSubmissionCheck(Applicant applicant, Opportunity opportunity) {
        List<String> failures = new ArrayList<>();

        ApplicantProfile profile = applicant.getProfile();
        if (profile == null || profile.getEducationLevel() == null
                || profile.getYearsOfExperience() == null || profile.getAvailability() == null) {
            failures.add("Applicant profile is incomplete");
        }
        if (!consentRepository.existsByApplicantIdAndStatus(applicant.getId(), ApplicantConsent.Status.ACTIVE)) {
            failures.add("No active consent on record");
        }
        checkRequiredDocuments(applicant, opportunity, failures);

        return failures;
    }

    public ReadinessResult opportunityEligibility(Long applicantId, Long opportunityId) {
        Applicant applicant = applicantService.getEntity(applicantId);
        Opportunity opportunity = opportunityService.getEntity(opportunityId);
        List<String> failures = new ArrayList<>();

        if (opportunity.getStatus() != Opportunity.Status.OPEN) {
            failures.add("Opportunity is not open for applications");
        }
        if (!ELIGIBLE_STAGES.contains(applicant.getLifecycleStage())) {
            failures.add("Applicant lifecycle stage is " + applicant.getLifecycleStage());
        }

        ApplicantProfile profile = applicant.getProfile();
        if (profile == null) {
            failures.add("Applicant profile is incomplete");
            return new ReadinessResult(false, failures);
        }

        if (profile.getAvailability() == ApplicantProfile.Availability.UNAVAILABLE) {
            failures.add("Applicant is marked unavailable");
        }

        if (opportunity.getMinimumAge() != null || opportunity.getMaximumAge() != null) {
            if (applicant.getDateOfBirth() == null) {
                failures.add("Date of birth missing; cannot verify age requirements");
            } else {
                int age = Period.between(applicant.getDateOfBirth(), LocalDate.now()).getYears();
                if (opportunity.getMinimumAge() != null && age < opportunity.getMinimumAge()) {
                    failures.add("Age " + age + " is below the required minimum " + opportunity.getMinimumAge());
                }
                if (opportunity.getMaximumAge() != null && age > opportunity.getMaximumAge()) {
                    failures.add("Age " + age + " exceeds the required maximum " + opportunity.getMaximumAge());
                }
            }
        }

        if (applicant.getApplicantType() == Applicant.ApplicantType.INTERNATIONAL
                && !profile.isWillingToRelocate()) {
            failures.add("Applicant is not willing to relocate");
        }

        Integer years = profile.getYearsOfExperience();
        if (years != null && opportunity.getRequiredExperience() != null
                && isInteger(opportunity.getRequiredExperience())) {
            int required = Integer.parseInt(opportunity.getRequiredExperience().trim());
            if (years < required) {
                failures.add("Experience " + years + " years is below required " + required);
            }
        }

        if (opportunity.getRequiredEducation() != null && !opportunity.getRequiredEducation().isBlank()
                && profile.getEducationLevel() == null) {
            failures.add("Education level missing but required");
        }

        if (opportunity.getRequiredLanguages() != null && !opportunity.getRequiredLanguages().isBlank()
                && (profile.getLanguages() == null || profile.getLanguages().isBlank())) {
            failures.add("Language information missing but required");
        }

        checkRequiredDocuments(applicant, opportunity, failures);

        return new ReadinessResult(failures.isEmpty(), failures);
    }

    private void checkRequiredDocuments(Applicant applicant, Opportunity opportunity, List<String> failures) {
        List<DocumentRequirement> requirements = requirementRepository
                .findByApplicantTypeAndOpportunityIsNullOrderByIdAsc(applicant.getApplicantType());
        if (opportunity != null) {
            requirements = new ArrayList<>(requirements);
            requirements.addAll(requirementRepository.findByOpportunityIdOrderByIdAsc(opportunity.getId()));
        }

        for (DocumentRequirement requirement : requirements) {
            if (!requirement.isRequired()) {
                continue;
            }
            Optional<ApplicantDocument> doc = applicantDocumentRepository
                    .findByApplicantIdAndDocumentTypeIdAndCurrentTrue(applicant.getId(),
                            requirement.getDocumentType().getId());
            if (doc.isEmpty() || !SUBMITTED_DOC_STATUSES.contains(doc.get().getStatus())) {
                failures.add("Missing required document: " + requirement.getDocumentType().getName());
            } else if (requirement.getDocumentType().isRequiresVerification()
                    && doc.get().getStatus() != ApplicantDocument.Status.VERIFIED) {
                failures.add("Document not yet verified: " + requirement.getDocumentType().getName());
            }
        }
    }

    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}