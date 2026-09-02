package com.starnet.SslAgency.placement.repository;

import com.starnet.SslAgency.placement.model.Placement;
import com.starnet.SslAgency.placement.model.Placement.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlacementRepository extends JpaRepository<Placement, Long> {
    List<Placement> findByContractId(Long contractId);
    List<Placement> findByApplicationId(Long applicationId);
    List<Placement> findByInterApplicationId(Long interApplicationId);
    long countByStage(Stage stage);
}
