package com.starnet.SslAgency.placement.core.repository;

import com.starnet.SslAgency.placement.core.model.Placement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CorePlacementRepository extends JpaRepository<Placement, Long> {

    Optional<Placement> findFirstByApplicantIdAndStageInOrderByCreatedAtDesc(Long applicantId, Collection<Placement.Stage> stages);

    Optional<Placement> findByApplicationId(Long applicationId);

    List<Placement> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);

    List<Placement> findByOpportunityIdOrderByCreatedAtDesc(Long opportunityId);

    List<Placement> findByStageOrderByCreatedAtDesc(Placement.Stage stage);

    List<Placement> findAllByStageInOrderByCreatedAtDesc(Collection<Placement.Stage> stages);
}