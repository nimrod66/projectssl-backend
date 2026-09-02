package com.starnet.SslAgency.recruitment.service;

import com.starnet.SslAgency.applicant.model.ApplicantTimeline;
import com.starnet.SslAgency.applicant.service.ApplicantTimelineService;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.processor.repository.StaffRepository;
import com.starnet.SslAgency.recruitment.dto.InterviewDto;
import com.starnet.SslAgency.recruitment.dto.InterviewResponseDto;
import com.starnet.SslAgency.recruitment.model.Application;
import com.starnet.SslAgency.recruitment.model.Interview;
import com.starnet.SslAgency.recruitment.repository.InterviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class InterviewService {

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ApplicantTimelineService timelineService;

    @Transactional
    public InterviewResponseDto schedule(InterviewDto dto, Staff actor) {
        Application application = applicationService.getEntity(dto.getApplicationId());
        if (application.getStatus() != Application.Status.SHORTLISTED
                && application.getStatus() != Application.Status.INTERVIEW) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Interviews can only be scheduled for shortlisted applications");
        }

        Interview interview = Interview.builder()
                .application(application)
                .type(dto.getType() != null ? dto.getType() : Interview.Type.SSL_SCREENING)
                .scheduledAt(dto.getScheduledAt() != null ? dto.getScheduledAt() : java.time.LocalDateTime.now())
                .interviewer(dto.getInterviewerId() != null
                        ? staffRepository.findById(dto.getInterviewerId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interviewer not found"))
                        : null)
                .location(dto.getLocation())
                .meetingLink(dto.getMeetingLink())
                .status(Interview.Status.SCHEDULED)
                .build();

        interview = interviewRepository.save(interview);

        if (application.getStatus() == Application.Status.SHORTLISTED) {
            applicationService.moveToInterview(application.getId());
        }

        timelineService.log(application.getApplicant(), ApplicantTimeline.EventType.INTERVIEW_SCHEDULED,
                "Interview scheduled: " + interview.getType().name()
                        + (interview.getInterviewer() != null
                        ? " with " + interview.getInterviewer().getFirstName()
                        : ""),
                actor, "interviewId=" + interview.getId());

        return InterviewResponseDto.from(interview);
    }

    @Transactional
    public InterviewResponseDto complete(Long id, Interview.Outcome outcome, Integer rating, String notes, Staff actor) {
        Interview interview = getEntity(id);
        requireStatus(interview, Interview.Status.SCHEDULED);

        interview.setStatus(Interview.Status.COMPLETED);
        interview.setOutcome(outcome != null ? outcome : Interview.Outcome.PENDING);
        interview.setRating(rating);
        interview.setNotes(notes);
        interviewRepository.save(interview);

        Application application = interview.getApplication();
        if (interview.getOutcome() == Interview.Outcome.FAIL) {
            applicationService.reject(application.getId(),
                    Application.RejectionReason.FAILED_INTERVIEW, "Failed interview", actor);
        } else {
            timelineService.log(application.getApplicant(), ApplicantTimeline.EventType.INTERVIEW_COMPLETED,
                    "Interview completed: " + interview.getOutcome().name()
                            + (rating != null ? " (rating " + rating + ")" : ""),
                    actor, "interviewId=" + interview.getId());
        }

        return InterviewResponseDto.from(interview);
    }

    @Transactional
    public InterviewResponseDto cancel(Long id, String reason, Staff actor) {
        Interview interview = getEntity(id);
        requireStatus(interview, Interview.Status.SCHEDULED);
        interview.setStatus(Interview.Status.CANCELLED);
        interview.setNotes(reason != null ? reason : interview.getNotes());
        interviewRepository.save(interview);
        return InterviewResponseDto.from(interview);
    }

    @Transactional
    public InterviewResponseDto miss(Long id, Staff actor) {
        Interview interview = getEntity(id);
        requireStatus(interview, Interview.Status.SCHEDULED);
        interview.setStatus(Interview.Status.MISSED);
        interviewRepository.save(interview);
        return InterviewResponseDto.from(interview);
    }

    @Transactional
    public InterviewResponseDto reschedule(Long id, InterviewDto dto, Staff actor) {
        Interview oldInterview = getEntity(id);
        requireStatus(oldInterview, Interview.Status.SCHEDULED);

        oldInterview.setStatus(Interview.Status.RESCHEDULED);
        interviewRepository.save(oldInterview);

        Interview replacement = Interview.builder()
                .application(oldInterview.getApplication())
                .type(oldInterview.getType())
                .scheduledAt(dto.getScheduledAt() != null
                        ? dto.getScheduledAt() : oldInterview.getScheduledAt().plusDays(1))
                .interviewer(oldInterview.getInterviewer())
                .location(dto.getLocation() != null ? dto.getLocation() : oldInterview.getLocation())
                .meetingLink(dto.getMeetingLink() != null ? dto.getMeetingLink() : oldInterview.getMeetingLink())
                .status(Interview.Status.SCHEDULED)
                .build();

        replacement = interviewRepository.save(replacement);

        timelineService.log(oldInterview.getApplication().getApplicant(),
                ApplicantTimeline.EventType.INTERVIEW_SCHEDULED,
                "Interview rescheduled to " + replacement.getScheduledAt(), actor,
                "fromInterviewId=" + id + ";toInterviewId=" + replacement.getId());

        return InterviewResponseDto.from(replacement);
    }

    public List<InterviewResponseDto> listByApplication(Long applicationId) {
        return interviewRepository.findByApplicationIdOrderByScheduledAtAsc(applicationId).stream()
                .map(InterviewResponseDto::from)
                .toList();
    }

    public Interview getEntity(Long id) {
        return interviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview not found"));
    }

    private void requireStatus(Interview interview, Interview.Status... expected) {
        for (Interview.Status status : expected) {
            if (interview.getStatus() == status) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid interview status " + interview.getStatus() + " for this operation");
    }
}