package com.starnet.SslAgency.interapplication.controller;


import com.starnet.SslAgency.interapplication.dto.*;
import com.starnet.SslAgency.interapplication.model.InterApplication;
import com.starnet.SslAgency.interapplication.repository.InterApplicationRepository;
import com.starnet.SslAgency.interapplication.service.InterApplicationService;
import com.starnet.SslAgency.media.model.MediaFile;
import com.starnet.SslAgency.processor.model.Staff;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/international")
public class InterApplicationController {
    @Autowired
    private InterApplicationService interApplicationService;

    @Autowired
    private InterApplicationRepository interApplicationRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<InterApplicationResponseDto> getAllInterApplications(@RequestParam(required = false) String status) {
        return (status != null ? interApplicationService.listByStatus(status) : interApplicationService.getAllInterApplications()).stream().map(this::toResponseDto).toList();
    }

    @GetMapping("/{id}/cv")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public ResponseEntity<InterApplicationCVDto> getCV(@PathVariable Long id) {
        InterApplicationCVDto dto = interApplicationService.generateCVDto(id);
        return ResponseEntity.ok(dto);
    }


    @PatchMapping("/{id}/vet")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public ResponseEntity<InterApplicationResponseDto> vet(@PathVariable Long id, Authentication auth) {
        Staff staff = getAuthenticatedStaff(auth);
        InterApplication interApp = interApplicationService.markVetted(id, staff.getId());
        return ResponseEntity.ok(toResponseDto(interApp));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<InterApplicationResponseDto> approve(@PathVariable Long id, Authentication auth) {
        Staff staff = getAuthenticatedStaff(auth);
        InterApplication interApp = interApplicationService.approve(id, staff.getId());
        return ResponseEntity.ok(toResponseDto(interApp));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public ResponseEntity<InterApplicationResponseDto> reject(@PathVariable Long id, Authentication auth) {
        Staff staff = getAuthenticatedStaff(auth);
        InterApplication interApp = interApplicationService.reject(id, staff.getId());
        return ResponseEntity.ok(toResponseDto(interApp));
    }

    @PatchMapping("/{id}/hired")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<InterApplicationResponseDto> markHired(@PathVariable Long id, Authentication auth) {
        Staff staff = getAuthenticatedStaff(auth);
        InterApplication interApp = interApplicationService.markHired(id, staff.getId());
        return ResponseEntity.ok(toResponseDto(interApp));
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<InterApplicationResponseDto> restoreToApproved(@PathVariable Long id, Authentication auth) {
        Staff staff = getAuthenticatedStaff(auth);
        InterApplication interApp = interApplicationService.restoreToApproved(id, staff.getId());
        return ResponseEntity.ok(toResponseDto(interApp));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteInternationalApplication(@PathVariable Long id) {
        interApplicationService.deleteInternationalApplication(id);
        return ResponseEntity.noContent().build();
    }

    private Staff getAuthenticatedStaff(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Staff staff)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated staff found");
        }
        return staff;
    }

    private InterApplicationResponseDto toResponseDto(InterApplication interA) {
        List<MediaFile> mediaFiles = interA.getMediaFiles() != null ? interA.getMediaFiles() : List.of();
        return InterApplicationResponseDto.builder()
                .id(interA.getId())
                .fullName(Stream.of(interA.getFirstName(), interA.getMiddleName(), interA.getLastName()).filter(s -> s != null && !s.isBlank()).reduce((s1, s2) -> s1 + " " + s2).orElse(""))
                .phoneNumber(interA.getPhoneNumber())
                .email(interA.getEmail())
                .dob(interA.getDob())
                .age(interA.getAge())
                .nationality(interA.getNationality())
                .religion(interA.getReligion())
                .maritalStatus(interA.getMaritalStatus() != null ? interA.getMaritalStatus().name() : null)
                .numberOfKids(interA.getNumberOfKids())
                .educationLevel(interA.getEducationLevel() != null ? interA.getEducationLevel().name() : null)
                .currentProfession(interA.getCurrentProfession())
                .currentSalary(interA.getCurrentSalary())
                .currentLocation(interA.getCurrentLocation())
                .languages(interA.getLanguages() != null ? interA.getLanguages().stream().map(Enum::name).toList() : List.of())
                .employmentStatus(interA.getEmploymentStatus() != null ? interA.getEmploymentStatus().name() : null)
                .jobRecruitment(interA.getJobRecruitment() != null ? interA.getJobRecruitment().name() : null)
                .status(interA.getStatus().name())
                .createdAt(interA.getCreatedAt() != null ? interA.getCreatedAt().toString() : null)
                .updatedAt(interA.getUpdatedAt() != null ? interA.getUpdatedAt().toString() : null)
                .vettedById(interA.getVettedBy() != null ? interA.getVettedBy().getId() : null)
                .vettedByName(interA.getVettedBy() != null ? interA.getVettedBy().getFirstName() + " " + interA.getVettedBy().getLastName() : null)
                .approvedById(interA.getApprovedBy() != null ? interA.getApprovedBy().getId() : null)
                .approvedByName(interA.getApprovedBy() != null ? interA.getApprovedBy().getFirstName() + " " + interA.getApprovedBy().getLastName() : null)
                .passportPhotos(mediaFiles.stream().filter(m -> m.getKind() == MediaFile.Kind.PASSPORT)
                        .map(MediaFile::getFileUrl).toList())
                .fullPhotos(mediaFiles.stream().filter(m -> m.getKind() == MediaFile.Kind.FULL_PHOTO)
                        .map(MediaFile::getFileUrl).toList())
                .nationalIdPhotos(mediaFiles.stream().filter(m -> m.getKind() == MediaFile.Kind.NATIONAL_ID)
                        .map(MediaFile::getFileUrl).toList())
                .resumes(mediaFiles.stream().filter(m -> m.getKind() == MediaFile.Kind.RESUME)
                        .map(MediaFile::getFileUrl).toList())
                .birthCertificates(mediaFiles.stream().filter(m -> m.getKind() == MediaFile.Kind.BIRTH_CERTIFICATE)
                        .map(MediaFile::getFileUrl).toList())
                .goodConducts(mediaFiles.stream().filter(m -> m.getKind() == MediaFile.Kind.GOOD_CONDUCT)
                        .map(MediaFile::getFileUrl).toList())
                .videos(mediaFiles.stream().filter(m -> m.getKind() == MediaFile.Kind.VIDEO)
                        .map(MediaFile::getFileUrl).toList())
                .showcasePhotos(mediaFiles.stream().filter(m -> m.getKind() == MediaFile.Kind.SHOWCASE_PHOTO)
                        .map(MediaFile::getFileUrl).toList()).build();


    }


}
