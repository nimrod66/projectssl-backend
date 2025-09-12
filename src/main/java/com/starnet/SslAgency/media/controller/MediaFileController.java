package com.starnet.SslAgency.media.controller;


import com.starnet.SslAgency.application.model.Application;
import com.starnet.SslAgency.application.repository.ApplicationRepository;
import com.starnet.SslAgency.media.model.MediaFile;
import com.starnet.SslAgency.media.service.MediaFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/media")
public class MediaFileController {
    @Autowired
    private MediaFileService mediaFileService;
    @Autowired
    private ApplicationRepository applicationRepository;

    @PostMapping(value = "/{applicationId}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaFile> uploadPhoto(
            @PathVariable Long applicationId,
            @RequestPart("file") MultipartFile file,
            @RequestParam MediaFile.Kind kind // PASSPORT, FULL_PHOTO, NATIONAL_ID
    ) throws IOException {
        if (kind == MediaFile.Kind.RESUME) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use the correct endpoint for resume");
        }
        return ResponseEntity.ok(mediaFileService.store(applicationId, file, kind));
    }

    @PostMapping(value = "/{applicationId}/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaFile> uploadResume(
            @PathVariable Long applicationId,
            @RequestPart("file") MultipartFile file,
            @RequestParam("kind") MediaFile.Kind kind //BIRTH_CERTIFICATE, RESUME, GOOD_CONDUCT
    ) throws IOException {
        MediaFile saved = mediaFileService.store(applicationId, file, kind);
        return ResponseEntity.ok(saved);
    }


    //Videos added after vetting by an admin
    @PostMapping(value = "/{applicationId}/videos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<MediaFile>> uploadVideos(@PathVariable Long applicationId, @RequestPart("files") List<MultipartFile> files) throws IOException {
        Application app = applicationRepository.findById(applicationId).orElseThrow(() -> new RuntimeException("Application not found"));
        if (app.getStatus() == Application.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Videos can only be added by after vetting");
        }
        List<MediaFile> saved = new ArrayList<>();
        for (MultipartFile f : files) {
            saved.add(mediaFileService.store(applicationId, f, MediaFile.Kind.VIDEO));
        }
        return ResponseEntity.ok(saved);
    }

    @PostMapping(value = "/{applicationId}/showcase", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<MediaFile>> uploadShowcasePhotos(
            @PathVariable Long applicationId,
            @RequestPart("files") List<MultipartFile> files) throws IOException {
        Application app = applicationRepository.findById(applicationId).orElseThrow(() -> new RuntimeException("Application not found"));
        if (app.getStatus() == Application.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Showcase Photos can only be added after vetting");

        }
        List<MediaFile> existing = mediaFileService.findByApplicationAndKind(applicationId, MediaFile.Kind.SHOWCASE_PHOTO);
        existing.forEach(mediaFileService::delete);

        List<MediaFile> saved = new ArrayList<>();
        for (MultipartFile f : files) {
            saved.add(mediaFileService.store(applicationId, f, MediaFile.Kind.SHOWCASE_PHOTO));
        }
        return ResponseEntity.ok(saved);
    }

}
