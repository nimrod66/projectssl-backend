package com.starnet.SslAgency.processor.controller;

import com.starnet.SslAgency.processor.dto.StaffResponseDto;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.processor.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {
    @Autowired
    private StaffService staffService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<StaffResponseDto> getAllStaff() {
        return staffService.getAllStaff()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST','APPLICANT')")
    public ResponseEntity<StaffResponseDto> getCurrentStaff(@AuthenticationPrincipal Staff currentStaff) {
        return ResponseEntity.ok(toResponseDto(currentStaff));
    }

    private StaffResponseDto toResponseDto(Staff staff) {
        return StaffResponseDto.builder()
                .id(staff.getId())
                .fullName(staff.getFirstName() + " " + staff.getLastName())
                .firstName(staff.getFirstName())
                .lastName(staff.getLastName())
                .email(staff.getEmail())
                .phoneNumber(staff.getPhoneNumber())
                .role(staff.getRole().name())
                .build();
    }
}
