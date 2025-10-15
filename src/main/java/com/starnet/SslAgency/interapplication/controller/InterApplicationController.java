package com.starnet.SslAgency.interapplication.controller;


import com.starnet.SslAgency.interapplication.dto.InterApplicationPublicDto;
import com.starnet.SslAgency.interapplication.dto.InterApplicationRequestDto;
import com.starnet.SslAgency.interapplication.dto.InterApplicationResponseDto;
import com.starnet.SslAgency.interapplication.model.InterApplication;
import com.starnet.SslAgency.interapplication.service.InterApplicationService;
import com.starnet.SslAgency.media.model.MediaFile;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.processor.repository.StaffRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/international")
public class InterApplicationController {
    @Autowired
    private InterApplicationService interApplicationService;

    @Autowired
    private StaffRepository staffRepository;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InterApplicationResponseDto> createApplication(@RequestBody @Valid InterApplicationRequestDto dto) {
        InterApplication interApplication = interApplicationService.createInterApplication(dto);
        return ResponseEntity.ok(toResponseDto(interApplication));
    }

    @GetMapping
    public List<InterApplicationResponseDto> getAllInterApplications() {
        return interApplicationService.getAllInterApplications().stream().map(this::toResponseDto).toList();
    }

    @GetMapping("/public")
    public List<InterApplicationPublicDto> getPublicInterApplications() {
        List<InterApplication> interApp = interApplicationService.getPublicInterApplications();
        return interApp.stream()
                .map(this::toPublicDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterApplicationResponseDto> getInterApplication(@PathVariable Long id) {
        InterApplication interApplication = interApplicationService.getInterApplication(id);
        return ResponseEntity.ok(toResponseDto(interApplication));
    }

    @PatchMapping("/{id}/vet")
    public ResponseEntity<InterApplicationResponseDto> vet(@PathVariable Long id, Authentication auth) {
        Staff staff = getAuthenticatedStaff(auth);
        InterApplication interApp = interApplicationService.markVetted(id, staff.getId());
        return ResponseEntity.ok(toResponseDto(interApp));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<InterApplicationResponseDto> approve(@PathVariable Long id, Authentication auth) {
        Staff staff = getAuthenticatedStaff(auth);
        InterApplication interApp = interApplicationService.approve(id, staff.getId());
        return ResponseEntity.ok(toResponseDto(interApp));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<InterApplicationResponseDto> reject(@PathVariable Long id, Authentication auth) {
        Staff staff = getAuthenticatedStaff(auth);
        InterApplication interApp = interApplicationService.reject(id, staff.getId());
        return ResponseEntity.ok(toResponseDto(interApp));
    }

    @PatchMapping("/{id}/hired")
    public ResponseEntity<InterApplicationResponseDto> markHired(@PathVariable Long id, Authentication auth) {
        Staff staff = getAuthenticatedStaff(auth);
        InterApplication interApp = interApplicationService.markHired(id, staff.getId());
        return ResponseEntity.ok(toResponseDto(interApp));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<InterApplicationResponseDto> restoreToApproved(@PathVariable Long id, Authentication auth) {
        Staff staff = getAuthenticatedStaff(auth);
        InterApplication interApp = interApplicationService.restoreToApproved(id, staff.getId());
        return ResponseEntity.ok(toResponseDto(interApp));
    }

    private Staff getAuthenticatedStaff(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Staff staff)) {
            throw new RuntimeException("No Authenticated staff found");
        }
        return staff;
    }

    private InterApplicationPublicDto toPublicDto(InterApplication interApp) {
        List<MediaFile> mediaFiles = interApp.getMediaFiles() != null ? interApp.getMediaFiles() : List.of();
        return InterApplicationPublicDto.builder()
                .id(interApp.getId())
                .fullName(Stream.of(interApp.getFirstName(), interApp.getMiddleName(), interApp.getLastName()).filter(s -> s != null && !s.isBlank()).reduce((s1, s2) -> s1 + " " + s2).orElse(""))
                .age(interApp.getAge())
                .nationality(interApp.getNationality())
                .currentProfession(interApp.getCurrentProfession())
                .currentLocation(interApp.getCurrentLocation())
                .languages(interApp.getLanguages() != null ? interApp.getLanguages().stream().map(Enum::name).toList() : List.of())
                .videos(mediaFiles.stream().filter(m -> m.getKind() == MediaFile.Kind.VIDEO)
                        .map(MediaFile::getFileUrl).toList())
                .showcasePhotos(mediaFiles.stream().filter(m -> m.getKind() == MediaFile.Kind.SHOWCASE_PHOTO)
                        .map(MediaFile::getFileUrl).toList())
                .build();
    }


    private InterApplicationResponseDto toResponseDto(InterApplication interA) {
        List<MediaFile> mediaFiles = interA.getMediaFiles() != null ? interA.getMediaFiles() : List.of();
        return InterApplicationResponseDto.builder()
                .id(interA.getId())
                .fullName(Stream.of(interA.getFirstName(), interA.getMiddleName(), interA.getLastName()).filter(s -> s != null && !s.isBlank()).reduce((s1, s2) -> s1 + " " + s2).orElse(""))
                .dob(interA.getDob())
                .age(interA.getAge())
                .nationality(interA.getNationality())
                .religion(interA.getReligion())
                .maritalStatus(interA.getMaritalStatus() != null ? interA.getMaritalStatus().name() : null)
                .numberOfKids(interA.getNumberOfKids())
                .educationLevel(interA.getEducationLevel() != null ? interA.getEducationLevel().name() : null)
                .currentProfession(interA.getCurrentProfession())
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
                .approvedByName(interA.getApprovedBy() != null ? interA.getApprovedBy().getFirstName() + " " + interA.getVettedBy().getLastName() : null)
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
