package com.starnet.SslAgency.applicant.controller;

import com.starnet.SslAgency.applicant.dto.ApplicantProfileDto;
import com.starnet.SslAgency.applicant.dto.ApplicantRequestDto;
import com.starnet.SslAgency.applicant.dto.ApplicantResponseDto;
import com.starnet.SslAgency.applicant.dto.ApplicantConsentDto;
import com.starnet.SslAgency.applicant.model.Applicant;
import com.starnet.SslAgency.applicant.model.ApplicantConsent;
import com.starnet.SslAgency.applicant.repository.ApplicantRepository;
import com.starnet.SslAgency.applicant.service.ApplicantService;
import com.starnet.SslAgency.applicant.service.ConsentService;
import com.starnet.SslAgency.applicant.service.ReadinessService;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applicants")
public class ApplicantController {

    @Autowired
    private ApplicantService applicantService;

    @Autowired
    private ConsentService consentService;

    @Autowired
    private ReadinessService readinessService;

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<ApplicantResponseDto> register(@RequestBody @Valid ApplicantRequestDto dto) {
        return ResponseEntity.ok(applicantService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String identifier = payload.get("identifier") != null ? payload.get("identifier").trim() : "";
        String password = payload.get("password");
        Applicant applicant = applicantRepository.findByEmail(identifier)
                .or(() -> applicantRepository.findByPhoneNumber(identifier))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (applicant.getPasswordHash() == null || !passwordEncoder.matches(password, applicant.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        if (applicant.getStatus() != Applicant.Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account is not active");
        }
        String token = jwtUtil.generateToken(applicant.getApplicantNumber(), "APPLICANT");
        return ResponseEntity.ok(Map.of(
                "token", token,
                "applicantNumber", applicant.getApplicantNumber(),
                "applicantType", applicant.getApplicantType().name(),
                "name", applicant.getFirstName() + " " + applicant.getLastName()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<ApplicantResponseDto> list() {
        return applicantService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public ApplicantResponseDto get(@PathVariable Long id) {
        return applicantService.get(id);
    }

    @GetMapping("/by-phone/{phone}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public ApplicantResponseDto getByPhone(@PathVariable String phone) {
        return applicantService.getByPhone(phone);
    }

    @PatchMapping("/{id}/profile")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public ApplicantResponseDto updateProfile(@PathVariable Long id, @RequestBody ApplicantProfileDto dto) {
        return applicantService.updateProfile(id, dto);
    }

    @PatchMapping("/{id}/recruiter")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public ApplicantResponseDto assignRecruiter(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        return applicantService.assignRecruiter(id, body.get("recruiterId"));
    }

    @PatchMapping("/{id}/lifecycle")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public ApplicantResponseDto transition(@PathVariable Long id,
                                           @RequestBody Map<String, String> body,
                                           @AuthenticationPrincipal Staff actor) {
        Applicant.LifecycleStage target = Applicant.LifecycleStage.valueOf(body.get("stage"));
        if (target == Applicant.LifecycleStage.INACTIVE || target == Applicant.LifecycleStage.BLACKLISTED) {
            boolean allowed = actor.getRole() == Staff.Role.ADMIN || actor.getRole() == Staff.Role.SUPER_ADMIN;
            if (!allowed) {
                throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Only administrators can perform this action");
            }
        }
        return applicantService.transition(id, target, body.get("reason"), actor);
    }

    @PostMapping("/{id}/consent")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public ApplicantConsentDto grantConsent(@PathVariable Long id,
                                            @RequestBody Map<String, String> body,
                                            @AuthenticationPrincipal Staff actor) {
        ApplicantConsent.ConsentType type = ApplicantConsent.ConsentType.valueOf(body.get("consentType"));
        ApplicantConsent.Source source = body.get("source") != null
                ? ApplicantConsent.Source.valueOf(body.get("source")) : null;
        return consentService.grant(id, type, body.get("termsVersion"), source, actor);
    }

    @GetMapping("/{id}/consent")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<ApplicantConsentDto> listConsent(@PathVariable Long id) {
        return consentService.list(id);
    }

    @PatchMapping("/consent/{consentId}/revoke")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public ApplicantConsentDto revokeConsent(@PathVariable Long consentId,
                                             @AuthenticationPrincipal Staff actor) {
        return consentService.revoke(consentId, actor);
    }

    @GetMapping("/{id}/readiness")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public ReadinessService.ReadinessResult readiness(@PathVariable Long id) {
        return readinessService.applicantReadiness(id);
    }

    @GetMapping("/{id}/eligibility/{opportunityId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public ReadinessService.ReadinessResult eligibility(@PathVariable Long id, @PathVariable Long opportunityId) {
        return readinessService.opportunityEligibility(id, opportunityId);
    }
}
