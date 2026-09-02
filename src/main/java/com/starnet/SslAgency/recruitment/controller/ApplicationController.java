package com.starnet.SslAgency.recruitment.controller;

import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.recruitment.dto.ApplicationResponseDto;
import com.starnet.SslAgency.recruitment.model.Application;
import com.starnet.SslAgency.recruitment.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController("recruitmentApplicationController")
@RequestMapping("/api/recruitment/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<ApplicationResponseDto> list(@RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return applicationService.listByStatus(Application.Status.valueOf(status));
        }
        return applicationService.listAll();
    }

    @GetMapping("/by-applicant/{applicantId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<ApplicationResponseDto> byApplicant(@PathVariable Long applicantId) {
        return applicationService.listByApplicant(applicantId);
    }

    @PatchMapping("/{id}/screen")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public ApplicationResponseDto screen(@PathVariable Long id) {
        return applicationService.screen(id);
    }

    @PatchMapping("/{id}/shortlist")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public ApplicationResponseDto shortlist(@PathVariable Long id) {
        return applicationService.shortlist(id);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public ApplicationResponseDto reject(@PathVariable Long id,
                                         @RequestBody Map<String, String> body,
                                         @AuthenticationPrincipal Staff actor) {
        Application.RejectionReason reason = body.get("reason") != null
                ? Application.RejectionReason.valueOf(body.get("reason")) : null;
        return applicationService.reject(id, reason, body.get("details"), actor);
    }
}
