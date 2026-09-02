package com.starnet.SslAgency.campaign.repository;

import com.starnet.SslAgency.campaign.model.CampaignMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampaignMemberRepository extends JpaRepository<CampaignMember, Long> {

    List<CampaignMember> findByCampaignIdOrderByAddedAtDesc(Long campaignId);

    List<CampaignMember> findByApplicantIdOrderByAddedAtDesc(Long applicantId);

    Optional<CampaignMember> findByCampaignIdAndApplicantId(Long campaignId, Long applicantId);

    void deleteByCampaignIdAndApplicantId(Long campaignId, Long applicantId);
}