package com.starnet.SslAgency.document.repository;

import com.starnet.SslAgency.document.model.ApplicantMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicantMediaRepository extends JpaRepository<ApplicantMedia, Long> {

    List<ApplicantMedia> findByApplicantIdOrderByUploadedAtDesc(Long applicantId);
}