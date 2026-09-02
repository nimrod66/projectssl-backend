package com.starnet.SslAgency.document.repository;

import com.starnet.SslAgency.document.model.PlacementDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CorePlacementDocumentRepository extends JpaRepository<PlacementDocument, Long> {

    List<PlacementDocument> findByPlacementIdOrderByUploadedAtDesc(Long placementId);
}