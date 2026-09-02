package com.starnet.SslAgency.placement.core.repository;

import com.starnet.SslAgency.placement.core.model.PlacementStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CorePlacementStatusHistoryRepository extends JpaRepository<PlacementStatusHistory, Long> {

    List<PlacementStatusHistory> findByPlacementIdOrderByCreatedAtAsc(Long placementId);
}