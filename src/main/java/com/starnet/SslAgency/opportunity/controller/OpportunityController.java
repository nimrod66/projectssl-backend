package com.starnet.SslAgency.opportunity.controller;

import com.starnet.SslAgency.opportunity.dto.OpportunityRequestDto;
import com.starnet.SslAgency.opportunity.dto.OpportunityResponseDto;
import com.starnet.SslAgency.opportunity.model.Opportunity;
import com.starnet.SslAgency.opportunity.service.OpportunityService;
import com.starnet.SslAgency.processor.model.Staff;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {

    @Autowired
    private OpportunityService opportunityService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public OpportunityResponseDto create(@RequestBody @Valid OpportunityRequestDto dto,
                                         @AuthenticationPrincipal Staff actor) {
        return opportunityService.create(dto, actor);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public OpportunityResponseDto update(@PathVariable Long id, @RequestBody OpportunityRequestDto dto) {
        return opportunityService.update(id, dto);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public OpportunityResponseDto transition(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return opportunityService.transition(id, Opportunity.Status.valueOf(body.get("status")));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<OpportunityResponseDto> list(@RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return opportunityService.listByStatus(Opportunity.Status.valueOf(status));
        }
        return opportunityService.listAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public OpportunityResponseDto get(@PathVariable Long id) {
        return opportunityService.get(id);
    }
}
