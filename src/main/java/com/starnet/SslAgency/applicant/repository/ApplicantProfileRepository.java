package com.starnet.SslAgency.applicant.repository;

import com.starnet.SslAgency.applicant.model.ApplicantProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicantProfileRepository extends JpaRepository<ApplicantProfile, Long> {

    Optional<ApplicantProfile> findByApplicantId(Long applicantId);
}
