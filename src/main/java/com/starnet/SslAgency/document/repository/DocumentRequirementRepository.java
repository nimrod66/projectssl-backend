package com.starnet.SslAgency.document.repository;

import com.starnet.SslAgency.applicant.model.Applicant;
import com.starnet.SslAgency.document.model.DocumentRequirement;
import com.starnet.SslAgency.opportunity.model.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRequirementRepository extends JpaRepository<DocumentRequirement, Long> {

    List<DocumentRequirement> findByApplicantTypeOrderByIdAsc(Applicant.ApplicantType applicantType);

    List<DocumentRequirement> findByOpportunityIdOrderByIdAsc(Long opportunityId);

    List<DocumentRequirement> findByApplicantTypeAndOpportunityIsNullOrderByIdAsc(Applicant.ApplicantType applicantType);

    List<DocumentRequirement> findByOpportunityIsNullOrderByIdAsc();
}