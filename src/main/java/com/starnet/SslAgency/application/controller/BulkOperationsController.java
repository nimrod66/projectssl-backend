package com.starnet.SslAgency.application.controller;

import com.starnet.SslAgency.application.service.ApplicationService;
import com.starnet.SslAgency.processor.model.Staff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/applications")
public class BulkOperationsController {

    @Autowired private ApplicationService applicationService;

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public Map<String, Object> bulkOperation(@RequestBody Map<String, Object> body,
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
                    case "approve" -> applicationService.approve(Long.valueOf(id), staff.getId());
                    case "reject" -> applicationService.reject(Long.valueOf(id), staff.getId());
                    case "vet" -> applicationService.markVetted(Long.valueOf(id), staff.getId());
                    case "archive" -> applicationService.deleteApplication(Long.valueOf(id));
                    default -> { failed++; continue; }
                }
                success++;
            } catch (Exception e) { failed++; }
        }

        return Map.of("success", success, "failed", failed);
    }
}
