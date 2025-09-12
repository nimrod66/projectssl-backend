package com.starnet.SslAgency.security.controller;

import com.starnet.SslAgency.processor.dto.StaffRequestDto;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.processor.repository.StaffRepository;
import com.starnet.SslAgency.processor.service.StaffService;
import com.starnet.SslAgency.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private StaffService staffService;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid StaffRequestDto dto) {
        Staff saved = staffService.createStaff(dto);
        return ResponseEntity.ok(Map.of("id", saved.getId(), "firstName", saved.getFirstName(), "lastName", saved.getLastName(), "phoneNumber", saved.getPhoneNumber(), "email", saved.getEmail(), "role", saved.getRole().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");
        Staff staff = staffRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Invalid Credentials"));
        if (!passwordEncoder.matches(password, staff.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtUtil.generateToken(staff.getEmail(), staff.getRole().name());
        return ResponseEntity.ok(Map.of("token", token, "role", staff.getRole().name()));
    }
}
