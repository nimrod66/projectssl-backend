package com.starnet.SslAgency.application.controller;


import com.starnet.SslAgency.application.dto.*;
import com.starnet.SslAgency.application.model.Application;
import com.starnet.SslAgency.application.service.ApplicationService;
import com.starnet.SslAgency.media.model.MediaFile;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.processor.repository.StaffRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private StaffRepository staffRepository;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApplicationResponseDto> createApplication(@RequestBody @Valid ApplicationRequestDto dto) {
        Application application = applicationService.createApplication(dto);
        return ResponseEntity.ok(toResponseDto(application));
    }

    @GetMapping
    public List<ApplicationResponseDto> getAllApplications() {
        return applicationService.getAllApplications().stream().map(this::toResponseDto).toList();
    }

    @GetMapping("/public")
    public List<ApplicationPublicDto> getPublicApplications() {
        List<Application> apps = applicationService.getPublicApplications();
        return apps.stream()
                .map(this::toPublicDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponseDto> getApplication(@PathVariable Long id) {
        Application application = applicationService.getApplication(id);
        return ResponseEntity.ok(toResponseDto(application));
    }

    @GetMapping("/search")
    public List<ApplicationSearchDto> search(@RequestParam String name) {
        return applicationService.searchByName(name);
    }

    @PostMapping("/filter")
    public List<ApplicationResponseDto> filterApplications(@RequestBody ApplicationFilterDto filter) {
        List<Application> filtered = applicationService.filterApplications(filter);
        return filtered.stream().map(this::toResponseDto).toList();
    }


    @PatchMapping("/{id}/vet")
    public ResponseEntity<ApplicationResponseDto> vet(@PathVariable Long id, Authentication auth) {
        Staff staff = getAuthenticatedStaff(auth);
        Application app = applicationService.markVetted(id, staff.getId());
        return ResponseEntity.ok(toResponseDto(app));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApplicationResponseDto> approve(@PathVariable Long id, Authentication auth) {
        Staff staff = getAuthenticatedStaff(auth);
        Application app = applicationService.approve(id, staff.getId());
        return ResponseEntity.ok(toResponseDto(app));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApplicationResponseDto> reject(@PathVariable Long id, Authentication auth) {
        Staff staff = getAuthenticatedStaff(auth);
        Application app = applicationService.reject(id, staff.getId());
        return ResponseEntity.ok(toResponseDto(app));
    }

    @PatchMapping("/{id}/hired")
    public ResponseEntity<ApplicationResponseDto> markHired(@PathVariable Long id, Authentication auth) {
        Staff staff = getAuthenticatedStaff(auth);
        Application app = applicationService.markHired(id, staff.getId());
        return ResponseEntity.ok(toResponseDto(app));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApplicationResponseDto> restoreToApproved(@PathVariable Long id, Authentication auth) {
        Staff staff = getAuthenticatedStaff(auth);
        Application app = applicationService.restoreToApproved(id, staff.getId());
        return ResponseEntity.ok(toResponseDto(app));
    }


    @GetMapping("/status/{status}")
    public Page<ApplicationResponseDto> listByStatus(@PathVariable String status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Page<Application> p = applicationService.listByStatus(status, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return p.map(this::toResponseDto);
    }

    private Staff getAuthenticatedStaff(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Staff staff)) {
            throw new RuntimeException("No authenticated staff found");
        }
        return staff;
    }


    private ApplicationPublicDto toPublicDto(Application app) {
        List<MediaFile> mediaFiles = app.getMediaFiles() != null ? app.getMediaFiles() : List.of();
        return ApplicationPublicDto.builder()
                .id(app.getId())
                .fullName(Stream.of(app.getFirstName(), app.getMiddleName(), app.getLastName()).filter(s -> s != null && !s.isBlank()).reduce((s1, s2) -> s1 + " " + s2).orElse(""))
                .age(app.getAge())
                .nationality(app.getNationality())
                .experience(app.getExperience())
                .currentLocation(app.getCurrentLocation())
                .languages(app.getLanguages() != null ? app.getLanguages().stream().map(Enum::name).toList() : List.of())
                .videos(mediaFiles.stream().filter(m -> m.getKind() == MediaFile.Kind.VIDEO)
                        .map(MediaFile::getFileUrl).toList())

                .showcasePhotos(mediaFiles.stream().filter(m -> m.getKind() == MediaFile.Kind.SHOWCASE_PHOTO)
                        .map(MediaFile::getFileUrl).toList())
                .hasCat(app.getHasCat())
                .hasDog(app.getHasDog())
                .extraPay(app.getExtraPay())
                .liveOut(app.getLiveOut())
                .privateRoom(app.getPrivateRoom())
                .elderlyCare(app.getElderlyCare())
                .specialNeeds(app.getSpecialNeeds())
                .olderThan1(app.getOlderThan1())
                .youngerThan1(app.getYoungerThan1())
                .build();

    }

    private ApplicationResponseDto toResponseDto(Application a) {
        List<MediaFile> mediaFiles = a.getMediaFiles() != null ? a.getMediaFiles() : List.of();
        return ApplicationResponseDto.builder().id(a.getId()).fullName(Stream.of(a.getFirstName(), a.getMiddleName(), a.getLastName()).filter(s -> s != null && !s.isBlank()).reduce((s1, s2) -> s1 + " " + s2).orElse("")).phoneNumber(a.getPhoneNumber()).email(a.getEmail()).dob(a.getDob()).age(a.getAge()).nationality(a.getNationality()).experience(a.getExperience()).currentSalary(a.getCurrentSalary()).currentProfession(a.getCurrentProfession()).currentLocation(a.getCurrentLocation()).languages(a.getLanguages() != null ? a.getLanguages().stream().map(Enum::name).toList() : List.of()).employmentStatus(a.getEmploymentStatus() != null ? a.getEmploymentStatus().name() : null).jobInterest(a.getJobInterest() != null ? a.getJobInterest().name() : null).status(a.getStatus().name()).createdAt(a.getCreatedAt() != null ? a.getCreatedAt().toString() : null).updatedAt(a.getUpdatedAt() != null ? a.getUpdatedAt().toString() : null)
                .vettedById(a.getVettedBy() != null ? a.getVettedBy().getId() : null)
                .vettedByName(a.getVettedBy() != null ? a.getVettedBy().getFirstName() + " " + a.getVettedBy().getLastName() : null)
                .approvedById(a.getApprovedBy() != null ? a.getApprovedBy().getId() : null)
                .approvedByName(a.getApprovedBy() != null ? a.getApprovedBy().getFirstName() + " " + a.getApprovedBy().getLastName() : null)

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

