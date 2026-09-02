package com.starnet.SslAgency.recruitment.controller;

import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.recruitment.dto.OfferRequestDto;
import com.starnet.SslAgency.recruitment.dto.OfferResponseDto;
import com.starnet.SslAgency.recruitment.service.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recruitment/offers")
public class OfferController {

    @Autowired
    private OfferService offerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public OfferResponseDto create(@RequestBody OfferRequestDto dto, @AuthenticationPrincipal Staff actor) {
        return offerService.create(dto, actor);
    }

    @GetMapping("/by-application/{applicationId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<OfferResponseDto> byApplication(@PathVariable Long applicationId) {
        return offerService.listByApplication(applicationId);
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public OfferResponseDto accept(@PathVariable Long id, @AuthenticationPrincipal Staff actor) {
        return offerService.accept(id, actor);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public OfferResponseDto reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body,
                                   @AuthenticationPrincipal Staff actor) {
        return offerService.reject(id, body != null ? body.get("reason") : null, actor);
    }

    @PatchMapping("/{id}/withdraw")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public OfferResponseDto withdraw(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body,
                                     @AuthenticationPrincipal Staff actor) {
        return offerService.withdraw(id, body != null ? body.get("reason") : null, actor);
    }

    @PatchMapping("/{id}/expire")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public OfferResponseDto expire(@PathVariable Long id, @AuthenticationPrincipal Staff actor) {
        return offerService.expire(id, actor);
    }
}