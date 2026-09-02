package com.starnet.SslAgency.placement.repository;

import com.starnet.SslAgency.placement.model.PlacementDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlacementDocumentRepository extends JpaRepository<PlacementDocument, Long> {
    List<PlacementDocument> findByPlacementId(Long placementId);
}
