package com.starnet.SslAgency.recruitment.repository;

import com.starnet.SslAgency.recruitment.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecruitmentApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);

    List<Application> findByOpportunityIdOrderByCreatedAtDesc(Long opportunityId);

    List<Application> findByApplicantIdAndOpportunityIdOrderByCreatedAtDesc(Long applicantId, Long opportunityId);

    List<Application> findByAssignedRecruiterIdAndStatusNot(Long recruiterId, Application.Status status);

    List<Application> findAllByOrderByCreatedAtDesc();

    List<Application> findByStatusOrderByCreatedAtDesc(Application.Status status);
}