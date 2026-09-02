package com.starnet.SslAgency.campaign.repository;

import com.starnet.SslAgency.campaign.model.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    List<Campaign> findByStatusOrderByCreatedAtDesc(Campaign.Status status);

    List<Campaign> findAllByOrderByCreatedAtDesc();
}