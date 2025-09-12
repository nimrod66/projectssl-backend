package com.starnet.SslAgency.processor.controller;

import com.starnet.SslAgency.processor.dto.StaffRequestDto;
import com.starnet.SslAgency.processor.dto.StaffResponseDto;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.processor.service.StaffService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")

public class StaffController {
    @Autowired
    private StaffService staffService;

    @PostMapping
    public ResponseEntity<StaffResponseDto> createStaff(@RequestBody @Valid StaffRequestDto dto) {
        Staff staff = staffService.createStaff(dto);
        return ResponseEntity.ok(toResponseDto(staff));
    }

    @GetMapping
    public List<StaffResponseDto> getAllStaff() {
        return staffService.getAllStaff()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StaffResponseDto> getStaff(@PathVariable Long id) {
        return ResponseEntity.ok(toResponseDto(staffService.getStaff(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StaffResponseDto> updateStaff(
            @PathVariable Long id, @RequestBody @Valid StaffRequestDto dto
    ) {
        return ResponseEntity.ok(toResponseDto(staffService.updateStaff(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long id) {
        staffService.deleteStaff(id);
        return ResponseEntity.noContent().build();
    }

    private StaffResponseDto toResponseDto(Staff staff) {
        return StaffResponseDto.builder()
                .id(staff.getId())
                .fullName(staff.getFirstName() + " " + staff.getLastName())
                .email(staff.getEmail())
                .phoneNumber(staff.getPhoneNumber())
                .role(staff.getRole().name())
                .build();
    }
}
