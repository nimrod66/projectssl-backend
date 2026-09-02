package com.starnet.SslAgency.applicant.service;

import com.starnet.SslAgency.applicant.model.Applicant;
import com.starnet.SslAgency.applicant.model.ApplicantTimeline;
import com.starnet.SslAgency.applicant.repository.ApplicantTimelineRepository;
import com.starnet.SslAgency.processor.model.Staff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicantTimelineService {

    @Autowired
    private ApplicantTimelineRepository timelineRepository;

    public void log(Applicant applicant, ApplicantTimeline.EventType eventType, String description, Staff performedBy, String metadata) {
        ApplicantTimeline entry = ApplicantTimeline.builder()
                .applicant(applicant)
                .eventType(eventType)
                .description(description)
                .performedBy(performedBy)
                .metadata(metadata)
                .build();
        timelineRepository.save(entry);
    }
}
