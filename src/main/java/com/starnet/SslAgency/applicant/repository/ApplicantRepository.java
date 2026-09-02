package com.starnet.SslAgency.applicant.repository;

import com.starnet.SslAgency.applicant.model.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    Optional<Applicant> findByApplicantNumber(String applicantNumber);

    Optional<Applicant> findByPhoneNumber(String phoneNumber);

    Optional<Applicant> findByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}
