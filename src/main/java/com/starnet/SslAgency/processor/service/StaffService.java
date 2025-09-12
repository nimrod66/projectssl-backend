package com.starnet.SslAgency.processor.service;

import com.starnet.SslAgency.processor.dto.StaffRequestDto;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.processor.repository.StaffRepository;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.*;

import java.util.List;

@Service
public class StaffService {
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Staff createStaff(StaffRequestDto dto) {
        if (staffRepository.existsByEmail(dto.getEmail()))
            throw new IllegalArgumentException("Email already in use");
        Staff staff = Staff.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .role(dto.getRole())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .build();

        return staffRepository.save(staff);
    }

    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    public Staff getStaff(Long id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found in records"));
    }

    public Staff updateStaff(Long id, StaffRequestDto dto) {
        Staff staff = getStaff(id);
        staff.setFirstName(dto.getFirstName());
        staff.setLastName(dto.getLastName());
        if (!staff.getEmail().equals(dto.getEmail()) && staffRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        staff.setEmail(dto.getEmail());
        staff.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getRole() != null) {
            staff.setRole(dto.getRole());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            staff.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        return staffRepository.save(staff);
    }

    public void deleteStaff(Long id) {
        staffRepository.deleteById(id);
    }


}
