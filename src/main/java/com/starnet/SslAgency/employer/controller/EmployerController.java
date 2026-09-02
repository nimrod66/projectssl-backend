package com.starnet.SslAgency.employer.controller;

import com.starnet.SslAgency.employer.dto.EmployerRequestDto;
import com.starnet.SslAgency.employer.dto.EmployerResponseDto;
import com.starnet.SslAgency.employer.model.Employer;
import com.starnet.SslAgency.employer.service.EmployerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employers")
public class EmployerController {

    @Autowired
    private EmployerService employerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<EmployerResponseDto> getAll(@RequestParam(required = false) String status) {
        return employerService.getAllEmployers(status).stream()
                .map(employerService::toResponseDto)
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public EmployerResponseDto getOne(@PathVariable Long id) {
        return employerService.toResponseDto(employerService.getEmployer(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public EmployerResponseDto create(@RequestBody @Valid EmployerRequestDto dto) {
        Employer saved = employerService.createEmployer(dto);
        return employerService.toResponseDto(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public EmployerResponseDto update(@PathVariable Long id, @RequestBody @Valid EmployerRequestDto dto) {
        Employer updated = employerService.updateEmployer(id, dto);
        return employerService.toResponseDto(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employerService.deleteEmployer(id);
        return ResponseEntity.noContent().build();
    }
}
