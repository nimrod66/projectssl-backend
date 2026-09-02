package com.starnet.SslAgency.applicant.repository;

import com.starnet.SslAgency.applicant.model.ApplicantConsent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicantConsentRepository extends JpaRepository<ApplicantConsent, Long> {

    List<ApplicantConsent> findByApplicantIdOrderBySignedAtDesc(Long applicantId);

    boolean existsByApplicantIdAndStatus(Long applicantId, ApplicantConsent.Status status);
}