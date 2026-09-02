package com.starnet.SslAgency.applicant.repository;

import com.starnet.SslAgency.applicant.model.ApplicantTimeline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicantTimelineRepository extends JpaRepository<ApplicantTimeline, Long> {

    List<ApplicantTimeline> findByApplicantIdOrderByCreatedAtAsc(Long applicantId);
}
