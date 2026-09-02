package com.starnet.SslAgency.application.controller;


import com.starnet.SslAgency.application.dto.*;
import com.starnet.SslAgency.application.model.Application;
import com.starnet.SslAgency.application.service.ApplicationService;
import com.starnet.SslAgency.media.model.MediaFile;
import com.starnet.SslAgency.processor.model.Staff;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/api/applications")
public class ApplicationController {
    @Autowired
    private ApplicationService applicationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<ApplicationResponseDto> getAllApplications(@RequestParam(required = false) String status) {
        return (status != null ? applicationService.listByStatus(status) : applicationService.getAllApplications()).stream().map(this::toResponseDto).toList();
    }

    @PatchMapping("/{id}/vet")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public ResponseEntity<ApplicationResponseDto> vet(@PathVariable Long id, Authentication auth) {
        Staff staff = getAuthenticatedStaff(auth);
        Application app = applicationService.markVetted(id, staff.getId());
        return ResponseEntity.ok(toResponseDto(app));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApplicationResponseDto> approve(@PathVariable Long id, Authentication auth) {
        Staff staff = getAuthenticatedStaff(auth);
        Application app = applicationService.approve(id, staff.getId());
        return ResponseEntity.ok(toResponseDto(app));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public ResponseEntity<ApplicationResponseDto> reject(@PathVariable Long id, Authentication auth) {
        Staff staff = getAuthenticatedStaff(auth);
        Application app = applicationService.reject(id, staff.getId());
        return ResponseEntity.ok(toResponseDto(app));
    }

    @PatchMapping("/{id}/hired")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApplicationResponseDto> markHired(@PathVariable Long id, Authentication auth) {
        Staff staff = getAuthenticatedStaff(auth);
        Application app = applicationService.markHired(id, staff.getId());
        return ResponseEntity.ok(toResponseDto(app));
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApplicationResponseDto> restoreToApproved(@PathVariable Long id, Authentication auth) {
        Staff staff = getAuthenticatedStaff(auth);
        Application app = applicationService.restoreToApproved(id, staff.getId());
        return ResponseEntity.ok(toResponseDto(app));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }

    private Staff getAuthenticatedStaff(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Staff staff)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated staff found");
        }
        return staff;
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

