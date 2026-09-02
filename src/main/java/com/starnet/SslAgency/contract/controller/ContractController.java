package com.starnet.SslAgency.contract.controller;

import com.starnet.SslAgency.contract.dto.AssignCandidateRequest;
import com.starnet.SslAgency.contract.dto.ContractRequestDto;
import com.starnet.SslAgency.contract.dto.ContractResponseDto;
import com.starnet.SslAgency.contract.model.Contract;
import com.starnet.SslAgency.contract.service.ContractService;
import com.starnet.SslAgency.placement.model.Placement;
import com.starnet.SslAgency.placement.service.PlacementService;
import com.starnet.SslAgency.processor.model.Staff;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @Autowired
    private PlacementService placementService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<ContractResponseDto> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long employerId) {
        return contractService.getAllContracts(status, employerId).stream()
                .map(contractService::toResponseDto)
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public ContractResponseDto getOne(@PathVariable Long id) {
        return contractService.toResponseDto(contractService.getContract(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ContractResponseDto create(@RequestBody @Valid ContractRequestDto dto, Authentication auth) {
        Staff staff = getStaff(auth);
        Contract saved = contractService.createContract(dto, staff.getId());
        return contractService.toResponseDto(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ContractResponseDto update(@PathVariable Long id, @RequestBody @Valid ContractRequestDto dto) {
        Contract updated = contractService.updateContract(id, dto);
        return contractService.toResponseDto(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contractService.deleteContract(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> assignCandidate(
            @PathVariable Long id,
            @RequestBody @Valid AssignCandidateRequest request,
            Authentication auth) {
        Staff staff = getStaff(auth);
        Placement placement = contractService.assignCandidate(id, request, staff.getId());
        return ResponseEntity.ok(placementService.toResponseDto(placement));
    }

    @GetMapping("/{id}/placements")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<?> getPlacements(@PathVariable Long id) {
        return placementService.getPlacementsByContract(id).stream()
                .map(placementService::toResponseDto)
                .toList();
    }

    private Staff getStaff(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Staff staff)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated staff found");
        }
        return staff;
    }
}
