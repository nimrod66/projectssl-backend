package com.starnet.SslAgency.opportunity.repository;

import com.starnet.SslAgency.opportunity.model.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    List<Opportunity> findByStatusOrderByCreatedAtDesc(Opportunity.Status status);

    List<Opportunity> findByEmployerIdOrderByCreatedAtDesc(Long employerId);

    List<Opportunity> findByStatusInOrderByCreatedAtDesc(List<Opportunity.Status> statuses);
}
