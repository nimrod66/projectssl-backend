package com.starnet.SslAgency.media.controller;

import com.starnet.SslAgency.application.model.Application;
import com.starnet.SslAgency.application.repository.ApplicationRepository;
import com.starnet.SslAgency.interapplication.model.InterApplication;
import com.starnet.SslAgency.interapplication.repository.InterApplicationRepository;
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
    @Autowired
    private InterApplicationRepository interApplicationRepository;

    @PostMapping(value = "/application/{applicationId}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaFile> uploadApplicationPhoto(
            @PathVariable Long applicationId,
            @RequestPart("file") MultipartFile file,
            @RequestParam MediaFile.Kind kind
    ) throws IOException {
        if (kind == MediaFile.Kind.RESUME) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use the resume endpoint for resumes");
        }
        return ResponseEntity.ok(mediaFileService.store(applicationId, file, kind));
    }

    @PostMapping(value = "/application/{applicationId}/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaFile> uploadApplicationResume(
            @PathVariable Long applicationId,
            @RequestPart("file") MultipartFile file,
            @RequestParam("kind") MediaFile.Kind kind
    ) throws IOException {
        return ResponseEntity.ok(mediaFileService.store(applicationId, file, kind));
    }

    @PostMapping(value = "/application/{applicationId}/videos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<MediaFile>> uploadApplicationVideos(
            @PathVariable Long applicationId,
            @RequestPart("files") List<MultipartFile> files
    ) throws IOException {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        if (app.getStatus() == Application.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Videos can only be added after vetting");
        }

        List<MediaFile> saved = new ArrayList<>();
        for (MultipartFile f : files) {
            saved.add(mediaFileService.store(applicationId, f, MediaFile.Kind.VIDEO));
        }
        return ResponseEntity.ok(saved);
    }

    @PostMapping(value = "/application/{applicationId}/showcase", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<MediaFile>> uploadApplicationShowcase(
            @PathVariable Long applicationId,
            @RequestPart("files") List<MultipartFile> files
    ) throws IOException {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        if (app.getStatus() == Application.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Showcase photos can only be added after vetting");
        }

        List<MediaFile> existing = mediaFileService.findByApplicationAndKind(applicationId, MediaFile.Kind.SHOWCASE_PHOTO);
        existing.forEach(mediaFileService::delete);

        List<MediaFile> saved = new ArrayList<>();
        for (MultipartFile f : files) {
            saved.add(mediaFileService.store(applicationId, f, MediaFile.Kind.SHOWCASE_PHOTO));
        }
        return ResponseEntity.ok(saved);
    }


    @PostMapping(value = "/inter/{interApplicationId}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaFile> uploadInterPhoto(
            @PathVariable Long interApplicationId,
            @RequestPart("file") MultipartFile file,
            @RequestParam MediaFile.Kind kind
    ) throws IOException {
        if (kind == MediaFile.Kind.RESUME) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use the resume endpoint for resumes");
        }
        return ResponseEntity.ok(mediaFileService.store(interApplicationId, file, kind));
    }

    @PostMapping(value = "/inter/{interApplicationId}/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaFile> uploadInterResume(
            @PathVariable Long interApplicationId,
            @RequestPart("file") MultipartFile file,
            @RequestParam("kind") MediaFile.Kind kind
    ) throws IOException {
        return ResponseEntity.ok(mediaFileService.store(interApplicationId, file, kind));
    }

    @PostMapping(value = "/inter/{interApplicationId}/videos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<MediaFile>> uploadInterVideos(
            @PathVariable Long interApplicationId,
            @RequestPart("files") List<MultipartFile> files
    ) throws IOException {
        InterApplication interApp = interApplicationRepository.findById(interApplicationId)
                .orElseThrow(() -> new RuntimeException("InterApplication not found"));
        if (interApp.getStatus() == InterApplication.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Videos can only be added after vetting");
        }

        List<MediaFile> saved = new ArrayList<>();
        for (MultipartFile f : files) {
            saved.add(mediaFileService.store(interApplicationId, f, MediaFile.Kind.VIDEO));
        }
        return ResponseEntity.ok(saved);
    }

    @PostMapping(value = "/inter/{interApplicationId}/showcase", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<MediaFile>> uploadInterShowcase(
            @PathVariable Long interApplicationId,
            @RequestPart("files") List<MultipartFile> files
    ) throws IOException {
        InterApplication interApp = interApplicationRepository.findById(interApplicationId)
                .orElseThrow(() -> new RuntimeException("InterApplication not found"));
        if (interApp.getStatus() == InterApplication.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Showcase photos can only be added after vetting");
        }

        List<MediaFile> existing = mediaFileService.findByApplicationAndKind(interApplicationId, MediaFile.Kind.SHOWCASE_PHOTO);
        existing.forEach(mediaFileService::delete);

        List<MediaFile> saved = new ArrayList<>();
        for (MultipartFile f : files) {
            saved.add(mediaFileService.store(interApplicationId, f, MediaFile.Kind.SHOWCASE_PHOTO));
        }
        return ResponseEntity.ok(saved);
    }
}
