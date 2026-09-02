package com.starnet.SslAgency.opportunity.controller;

import com.starnet.SslAgency.opportunity.dto.OpportunityResponseDto;
import com.starnet.SslAgency.opportunity.service.OpportunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/opportunities")
public class PublicOpportunityController {

    @Autowired
    private OpportunityService opportunityService;

    @GetMapping
    public List<OpportunityResponseDto> listOpen() {
        return opportunityService.listPublicOpen();
    }

    @GetMapping("/{id}")
    public OpportunityResponseDto get(@PathVariable Long id) {
        return opportunityService.getPublic(id);
    }
}
