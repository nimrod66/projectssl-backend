package com.starnet.SslAgency.document.repository;

import com.starnet.SslAgency.document.model.ApplicantDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicantDocumentRepository extends JpaRepository<ApplicantDocument, Long> {

    List<ApplicantDocument> findByApplicantIdOrderByDocumentTypeAscVersionDesc(Long applicantId);

    Optional<ApplicantDocument> findByApplicantIdAndDocumentTypeIdAndCurrentTrue(Long applicantId, Long documentTypeId);

    List<ApplicantDocument> findByApplicantIdAndCurrentTrueOrderByDocumentTypeAsc(Long applicantId);
}