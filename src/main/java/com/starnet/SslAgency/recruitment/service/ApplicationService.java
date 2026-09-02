package com.starnet.SslAgency.recruitment.service;

import com.starnet.SslAgency.applicant.model.Applicant;
import com.starnet.SslAgency.applicant.model.ApplicantTimeline;
import com.starnet.SslAgency.applicant.service.ApplicantService;
import com.starnet.SslAgency.applicant.service.ApplicantTimelineService;
import com.starnet.SslAgency.applicant.service.ReadinessService;
import com.starnet.SslAgency.opportunity.model.Opportunity;
import com.starnet.SslAgency.opportunity.service.OpportunityService;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.processor.repository.StaffRepository;
import com.starnet.SslAgency.recruitment.dto.ApplicationResponseDto;
import com.starnet.SslAgency.recruitment.model.Application;
import com.starnet.SslAgency.recruitment.repository.RecruitmentApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service("recruitmentApplicationService")
public class ApplicationService {

    private static final Set<Application.Status> ACTIVE_STATUSES = Set.of(
            Application.Status.SUBMITTED, Application.Status.SCREENING,
            Application.Status.SHORTLISTED, Application.Status.INTERVIEW,
            Application.Status.OFFERED, Application.Status.ACCEPTED);

    @Autowired
    private RecruitmentApplicationRepository applicationRepository;

    @Autowired
    private ApplicantService applicantService;

    @Autowired
    private OpportunityService opportunityService;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ApplicantTimelineService timelineService;

    @Autowired
    private ReadinessService readinessService;

    @Transactional
    public ApplicationResponseDto apply(Long applicantId, Long opportunityId, Long assignedRecruiterId) {
        Applicant applicant = applicantService.getEntity(applicantId);
        if (applicant.getStatus() == Applicant.Status.BLACKLISTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Blacklisted applicant cannot apply");
        }

        Opportunity opportunity = opportunityService.getEntity(opportunityId);
        if (opportunity.getStatus() != Opportunity.Status.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Opportunity is not open for applications");
        }
        if (opportunity.getApplicationDeadline() != null
                && opportunity.getApplicationDeadline().atStartOfDay().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application deadline has passed");
        }
        if (opportunity.getFilledPositions() >= opportunity.getNumberOfPositions()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Opportunity is fully filled");
        }

        boolean hasActive = applicationRepository
                .findByApplicantIdAndOpportunityIdOrderByCreatedAtDesc(applicantId, opportunityId)
                .stream().anyMatch(a -> ACTIVE_STATUSES.contains(a.getStatus()));
        if (hasActive) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Applicant already has an active application for this opportunity");
        }

        List<String> preSubmissionFailures = readinessService.preSubmissionCheck(applicant, opportunity);
        if (!preSubmissionFailures.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Application prerequisites not met: " + String.join("; ", preSubmissionFailures));
        }

        Application application = Application.builder()
                .applicant(applicant)
                .opportunity(opportunity)
                .assignedRecruiter(assignedRecruiterId != null
                        ? staffRepository.findById(assignedRecruiterId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recruiter not found"))
                        : null)
                .status(Application.Status.SUBMITTED)
                .build();

        application = applicationRepository.save(application);

        timelineService.log(applicant, ApplicantTimeline.EventType.APPLICATION_SUBMITTED,
                "Applied to opportunity: " + opportunity.getTitle(), null,
                "opportunityId=" + opportunityId + ";applicationId=" + application.getId());

        return ApplicationResponseDto.from(application);
    }

    @Transactional
    public ApplicationResponseDto screen(Long id) {
        Application application = getEntity(id);
        requireStatus(application, Application.Status.SUBMITTED, Application.Status.SCREENING);
        application.setStatus(Application.Status.SCREENING);
        touch(application);
        return ApplicationResponseDto.from(applicationRepository.save(application));
    }

    @Transactional
    public ApplicationResponseDto shortlist(Long id) {
        Application application = getEntity(id);
        requireStatus(application, Application.Status.SCREENING, Application.Status.SHORTLISTED);
        application.setStatus(Application.Status.SHORTLISTED);
        touch(application);
        return ApplicationResponseDto.from(applicationRepository.save(application));
    }

    @Transactional
    public ApplicationResponseDto reject(Long id, Application.RejectionReason reason, String details, Staff actor) {
        Application application = getEntity(id);
        if (application.getStatus() == Application.Status.PLACED
                || application.getStatus() == Application.Status.REJECTED
                || application.getStatus() == Application.Status.WITHDRAWN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Application already terminal: " + application.getStatus());
        }
        application.setStatus(Application.Status.REJECTED);
        application.setRejectionReason(reason != null ? reason : Application.RejectionReason.OTHER);
        application.setRejectionDetails(details);
        touch(application);
        applicationRepository.save(application);

        timelineService.log(application.getApplicant(), ApplicantTimeline.EventType.APPLICATION_STATUS_CHANGED,
                "Application rejected for " + application.getOpportunity().getTitle()
                        + " - " + application.getRejectionReason(), actor,
                "reason=" + reason + ";applicationId=" + application.getId());

        return ApplicationResponseDto.from(application);
    }

    @Transactional
    public ApplicationResponseDto withdraw(Long id, String reason) {
        Application application = getEntity(id);
        if (application.getStatus() == Application.Status.PLACED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Placed applications cannot be withdrawn");
        }
        if (application.getStatus() == Application.Status.REJECTED
                || application.getStatus() == Application.Status.WITHDRAWN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application already terminal");
        }
        application.setStatus(Application.Status.WITHDRAWN);
        application.setRejectionReason(Application.RejectionReason.CANDIDATE_WITHDREW);
        application.setRejectionDetails(reason);
        touch(application);
        return ApplicationResponseDto.from(applicationRepository.save(application));
    }

    @Transactional
    public void markOffered(Long applicationId) {
        Application application = getEntity(applicationId);
        requireStatus(application, Application.Status.INTERVIEW, Application.Status.OFFERED);
        if (application.getStatus() != Application.Status.OFFERED) {
            application.setStatus(Application.Status.OFFERED);
            touch(application);
            applicationRepository.save(application);
        }
    }

    @Transactional
    public void acceptOffer(Long applicationId) {
        Application application = getEntity(applicationId);
        requireStatus(application, Application.Status.OFFERED);
        application.setStatus(Application.Status.ACCEPTED);
        touch(application);
        applicationRepository.save(application);
    }

    @Transactional
    public void markPlaced(Long applicationId) {
        Application application = getEntity(applicationId);
        requireStatus(application, Application.Status.ACCEPTED);
        application.setStatus(Application.Status.PLACED);
        touch(application);
        applicationRepository.save(application);
    }

    @Transactional
    public void moveToInterview(Long applicationId) {
        Application application = getEntity(applicationId);
        requireStatus(application, Application.Status.SHORTLISTED);
        application.setStatus(Application.Status.INTERVIEW);
        touch(application);
        applicationRepository.save(application);
    }

    public List<ApplicationResponseDto> listByApplicant(Long applicantId) {
        return applicationRepository.findByApplicantIdOrderByCreatedAtDesc(applicantId).stream()
                .map(ApplicationResponseDto::from)
                .toList();
    }

    public List<ApplicationResponseDto> listByOpportunity(Long opportunityId) {
        return applicationRepository.findByOpportunityIdOrderByCreatedAtDesc(opportunityId).stream()
                .map(ApplicationResponseDto::from)
                .toList();
    }

    public List<ApplicationResponseDto> listByRecruiter(Long recruiterId) {
        return applicationRepository.findByAssignedRecruiterIdAndStatusNot(recruiterId, Application.Status.WITHDRAWN)
                .stream()
                .map(ApplicationResponseDto::from)
                .toList();
    }

    public List<ApplicationResponseDto> listAll() {
        return applicationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ApplicationResponseDto::from)
                .toList();
    }

    public List<ApplicationResponseDto> listByStatus(Application.Status status) {
        return applicationRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(ApplicationResponseDto::from)
                .toList();
    }

    public ApplicationResponseDto get(Long id) {
        return ApplicationResponseDto.from(getEntity(id));
    }

    public Application getEntity(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
    }

    private void requireStatus(Application application, Application.Status... expected) {
        for (Application.Status status : expected) {
            if (application.getStatus() == status) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid application status " + application.getStatus() + " for this operation");
    }

    private void touch(Application application) {
        application.setLastActivityAt(LocalDateTime.now());
    }
}
