package com.starnet.SslAgency.placement.repository;

import com.starnet.SslAgency.placement.model.PlacementStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlacementStatusHistoryRepository extends JpaRepository<PlacementStatusHistory, Long> {
    List<PlacementStatusHistory> findByPlacementIdOrderByChangedAtDesc(Long placementId);
}
