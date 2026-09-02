package com.starnet.SslAgency.campaign.controller;

import com.starnet.SslAgency.campaign.dto.CampaignRequestDto;
import com.starnet.SslAgency.campaign.dto.CampaignResponseDto;
import com.starnet.SslAgency.campaign.model.Campaign;
import com.starnet.SslAgency.campaign.service.CampaignService;
import com.starnet.SslAgency.processor.model.Staff;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    @Autowired
    private CampaignService campaignService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public CampaignResponseDto create(@RequestBody @Valid CampaignRequestDto dto,
                                      @AuthenticationPrincipal Staff actor) {
        return campaignService.create(dto, actor);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public CampaignResponseDto update(@PathVariable Long id, @RequestBody CampaignRequestDto dto) {
        return campaignService.update(id, dto);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public CampaignResponseDto transition(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return campaignService.transition(id, Campaign.Status.valueOf(body.get("status")));
    }

    @PostMapping("/{id}/applicants")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public CampaignResponseDto addApplicant(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        return campaignService.addApplicant(id, body.get("applicantId"));
    }

    @DeleteMapping("/{id}/applicants/{applicantId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public CampaignResponseDto removeApplicant(@PathVariable Long id, @PathVariable Long applicantId) {
        return campaignService.removeApplicant(id, applicantId);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<CampaignResponseDto> list(@RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return campaignService.listByStatus(Campaign.Status.valueOf(status));
        }
        return campaignService.listAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public CampaignResponseDto get(@PathVariable Long id) {
        return campaignService.get(id);
    }

    @GetMapping("/by-applicant/{applicantId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<CampaignResponseDto> byApplicant(@PathVariable Long applicantId) {
        return campaignService.listForApplicant(applicantId);
    }
}