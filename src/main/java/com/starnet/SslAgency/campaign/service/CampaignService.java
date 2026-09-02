package com.starnet.SslAgency.campaign.service;

import com.starnet.SslAgency.applicant.model.Applicant;
import com.starnet.SslAgency.applicant.service.ApplicantService;
import com.starnet.SslAgency.campaign.dto.CampaignMemberDto;
import com.starnet.SslAgency.campaign.dto.CampaignRequestDto;
import com.starnet.SslAgency.campaign.dto.CampaignResponseDto;
import com.starnet.SslAgency.campaign.model.Campaign;
import com.starnet.SslAgency.campaign.model.CampaignMember;
import com.starnet.SslAgency.campaign.repository.CampaignMemberRepository;
import com.starnet.SslAgency.campaign.repository.CampaignRepository;
import com.starnet.SslAgency.processor.model.Staff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CampaignService {

    private static final Map<Campaign.Status, Set<Campaign.Status>> ALLOWED_TRANSITIONS;

    static {
        ALLOWED_TRANSITIONS = new EnumMap<>(Campaign.Status.class);
        ALLOWED_TRANSITIONS.put(Campaign.Status.DRAFT,
                Set.of(Campaign.Status.ACTIVE, Campaign.Status.CANCELLED));
        ALLOWED_TRANSITIONS.put(Campaign.Status.ACTIVE,
                Set.of(Campaign.Status.PAUSED, Campaign.Status.COMPLETED, Campaign.Status.CANCELLED));
        ALLOWED_TRANSITIONS.put(Campaign.Status.PAUSED,
                Set.of(Campaign.Status.ACTIVE, Campaign.Status.CANCELLED));
    }

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CampaignMemberRepository memberRepository;

    @Autowired
    private ApplicantService applicantService;

    @Transactional
    public CampaignResponseDto create(CampaignRequestDto dto, Staff createdBy) {
        Campaign campaign = Campaign.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .targetApplicantType(dto.getTargetApplicantType())
                .status(Campaign.Status.DRAFT)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .createdBy(createdBy)
                .build();
        campaign = campaignRepository.save(campaign);
        return buildResponse(campaign);
    }

    @Transactional
    public CampaignResponseDto update(Long id, CampaignRequestDto dto) {
        Campaign campaign = getEntity(id);
        campaign.setName(dto.getName());
        campaign.setDescription(dto.getDescription());
        campaign.setTargetApplicantType(dto.getTargetApplicantType());
        campaign.setStartDate(dto.getStartDate());
        campaign.setEndDate(dto.getEndDate());
        campaignRepository.save(campaign);
        return buildResponse(campaign);
    }

    @Transactional
    public CampaignResponseDto transition(Long id, Campaign.Status target) {
        Campaign campaign = getEntity(id);
        if (!ALLOWED_TRANSITIONS.getOrDefault(campaign.getStatus(), Set.of()).contains(target)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid campaign status transition from " + campaign.getStatus() + " to " + target);
        }
        campaign.setStatus(target);
        campaignRepository.save(campaign);
        return buildResponse(campaign);
    }

    @Transactional
    public CampaignResponseDto addApplicant(Long campaignId, Long applicantId) {
        Campaign campaign = getEntity(campaignId);
        if (campaign.getStatus() == Campaign.Status.COMPLETED || campaign.getStatus() == Campaign.Status.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign is " + campaign.getStatus());
        }
        if (memberRepository.findByCampaignIdAndApplicantId(campaignId, applicantId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Applicant already in campaign");
        }
        Applicant applicant = applicantService.getEntity(applicantId);
        CampaignMember member = CampaignMember.builder()
                .campaign(campaign)
                .applicant(applicant)
                .build();
        memberRepository.save(member);
        return buildResponse(campaign);
    }

    @Transactional
    public CampaignResponseDto removeApplicant(Long campaignId, Long applicantId) {
        Campaign campaign = getEntity(campaignId);
        memberRepository.deleteByCampaignIdAndApplicantId(campaignId, applicantId);
        return buildResponse(campaign);
    }

    public CampaignResponseDto get(Long id) {
        return buildResponse(getEntity(id));
    }

    public List<CampaignResponseDto> listAll() {
        return campaignRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::buildResponse)
                .toList();
    }

    public List<CampaignResponseDto> listByStatus(Campaign.Status status) {
        return campaignRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(this::buildResponse)
                .toList();
    }

    public List<CampaignResponseDto> listForApplicant(Long applicantId) {
        return memberRepository.findByApplicantIdOrderByAddedAtDesc(applicantId).stream()
                .map(m -> buildResponse(m.getCampaign()))
                .toList();
    }

    public Campaign getEntity(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
    }

    private CampaignResponseDto buildResponse(Campaign campaign) {
        List<CampaignMemberDto> members = memberRepository.findByCampaignIdOrderByAddedAtDesc(campaign.getId())
                .stream()
                .map(CampaignMemberDto::from)
                .toList();
        return CampaignResponseDto.from(campaign, members);
    }
}