package com.starnet.SslAgency.interapplication.controller;

import com.starnet.SslAgency.interapplication.service.InterApplicationService;
import com.starnet.SslAgency.processor.model.Staff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/international")
public class InternationalBulkController {

    @Autowired
    private InterApplicationService interApplicationService;

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public Map<String, Object> bulk(@RequestBody Map<String, Object> body,
                                    @AuthenticationPrincipal Staff staff) {
        if (staff == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated staff found");
        }
        List<Integer> ids = (List<Integer>) body.getOrDefault("ids", List.of());
        String action = (String) body.get("action");

        int success = 0;
        int failed = 0;

        for (Integer id : ids) {
            try {
                switch (action != null ? action.toLowerCase() : "") {
                    case "approve" -> interApplicationService.approve(Long.valueOf(id), staff.getId());
                    case "reject" -> interApplicationService.reject(Long.valueOf(id), staff.getId());
                    case "vet" -> interApplicationService.markVetted(Long.valueOf(id), staff.getId());
                    case "archive" -> interApplicationService.deleteInternationalApplication(Long.valueOf(id));
                    default -> { failed++; continue; }
                }
                success++;
            } catch (Exception e) { failed++; }
        }

        return Map.of("success", success, "failed", failed);
    }
}
