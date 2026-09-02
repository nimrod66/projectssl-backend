package com.starnet.SslAgency.placement.core.controller;

import com.starnet.SslAgency.placement.core.dto.PlacementChecklistDto;
import com.starnet.SslAgency.placement.core.dto.PlacementRequestDto;
import com.starnet.SslAgency.placement.core.dto.PlacementResponseDto;
import com.starnet.SslAgency.placement.core.model.Placement;
import com.starnet.SslAgency.placement.core.model.PlacementChecklist;
import com.starnet.SslAgency.placement.core.service.PlacementService;
import com.starnet.SslAgency.processor.model.Staff;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController("corePlacementController")
@RequestMapping("/api/recruitment/placements")
public class PlacementController {

    @Autowired
    private PlacementService placementService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public PlacementResponseDto create(@RequestBody @Valid PlacementRequestDto dto,
                                       @AuthenticationPrincipal Staff actor) {
        return placementService.create(dto, actor);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<PlacementResponseDto> list(@RequestParam(required = false) String stage) {
        if (stage != null && !stage.isBlank()) {
            return placementService.listByStage(Placement.Stage.valueOf(stage));
        }
        return placementService.listAll();
    }

    @PatchMapping("/{id}/stage")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public PlacementResponseDto transition(@PathVariable Long id, @RequestBody Map<String, String> body,
                                           @AuthenticationPrincipal Staff actor) {
        Placement.Stage target = Placement.Stage.valueOf(body.get("stage"));
        return placementService.transition(id, target, body.get("reason"), actor);
    }

    @PatchMapping("/{id}/checklist")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public PlacementChecklistDto updateCheckItem(@PathVariable Long id,
                                                 @RequestBody Map<String, String> body,
                                                 @AuthenticationPrincipal Staff actor) {
        PlacementChecklist.CheckItem item = PlacementChecklist.CheckItem.valueOf(body.get("item"));
        boolean completed = Boolean.parseBoolean(body.get("completed"));
        return placementService.updateCheckItem(id, item, completed, body.get("notes"), actor);
    }
}
