package com.starnet.SslAgency.placement.controller;

import com.starnet.SslAgency.placement.dto.PlacementResponseDto;
import com.starnet.SslAgency.placement.dto.PlacementStageRequest;
import com.starnet.SslAgency.placement.model.Placement;
import com.starnet.SslAgency.placement.service.PlacementService;
import com.starnet.SslAgency.processor.model.Staff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/placements")
public class PlacementController {

    @Autowired
    private PlacementService placementService;

    @PatchMapping("/{id}/stage")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public PlacementResponseDto advanceStage(
            @PathVariable Long id,
            @RequestBody PlacementStageRequest request,
            @AuthenticationPrincipal Staff staff) {
        Placement updated = placementService.advanceStage(id, request.getStage(), request.getNote(), staff.getId());
        return placementService.toResponseDto(updated);
    }

    @PostMapping("/{id}/notes")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public PlacementResponseDto addNote(
            @PathVariable Long id,
            @RequestBody PlacementStageRequest request,
            @AuthenticationPrincipal Staff staff) {
        Placement updated = placementService.addNote(id, request.getNote(), staff.getId());
        return placementService.toResponseDto(updated);
    }
}
