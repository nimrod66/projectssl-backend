package com.starnet.SslAgency.placement.core.repository;

import com.starnet.SslAgency.placement.core.model.PlacementChecklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlacementChecklistRepository extends JpaRepository<PlacementChecklist, Long> {

    List<PlacementChecklist> findByPlacementIdOrderByItemAsc(Long placementId);

    boolean existsByPlacementIdAndItem(Long placementId, PlacementChecklist.CheckItem item);
}