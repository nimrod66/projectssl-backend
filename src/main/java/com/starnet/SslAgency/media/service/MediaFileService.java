package com.starnet.SslAgency.media.service;

import com.starnet.SslAgency.application.model.Application;
import com.starnet.SslAgency.application.repository.ApplicationRepository;
import com.starnet.SslAgency.interapplication.model.InterApplication;
import com.starnet.SslAgency.interapplication.repository.InterApplicationRepository;
import com.starnet.SslAgency.media.model.MediaFile;
import com.starnet.SslAgency.media.repository.MediaFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

@Service
public class MediaFileService {

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private InterApplicationRepository interApplicationRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public MediaFile store(Long applicationId, MultipartFile file, MediaFile.Kind kind) throws IOException {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NoSuchElementException("Application not found"));
        return saveFile(file, kind, app, null);
    }

    public MediaFile storeInter(Long interApplicationId, MultipartFile file, MediaFile.Kind kind) throws IOException {
        InterApplication interApp = interApplicationRepository.findById(interApplicationId)
                .orElseThrow(() -> new NoSuchElementException("International Application not found"));
        return saveFile(file, kind, null, interApp);
    }

    private MediaFile saveFile(MultipartFile file, MediaFile.Kind kind,
                               Application app, InterApplication interApp) throws IOException {

        String original = Objects.requireNonNull(file.getOriginalFilename());
        String safeOriginal = original.replaceAll("\\s+", "_");
        String ext = getFileExtension(safeOriginal);
        validateFileExtension(kind, ext, file.getSize());

        FileStorageService.StoredFile stored = fileStorageService.store(file);

        MediaFile media = MediaFile.builder()
                .fileName(safeOriginal)
                .fileType(file.getContentType())
                .fileUrl(stored.fileUrl())
                .kind(kind)
                .application(app)
                .interApplication(interApp)
                .build();

        return mediaFileRepository.save(media);
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        return (dotIndex != -1) ? filename.substring(dotIndex).toLowerCase() : "";
    }

    private static final Set<String> IMAGE_EXT = Set.of(".jpg", ".jpeg", ".png", ".webp", ".heic");
    private static final Set<String> DOC_EXT = Set.of(".pdf", ".doc", ".docx");
    private static final Set<String> VIDEO_EXT = Set.of(".mp4", ".webm", ".mov");

    private void validateFileExtension(MediaFile.Kind kind, String ext, long size) {
        switch (kind) {
            case PASSPORT, FULL_PHOTO, SHOWCASE_PHOTO:
                if (!IMAGE_EXT.contains(ext))
                    throw new IllegalArgumentException("Only JPG, PNG, WebP, HEIC allowed for " + kind);
                if (size > 8 * 1024 * 1024)
                    throw new IllegalArgumentException(kind + " must be under 8 MB");
                break;
            case RESUME, NATIONAL_ID, BIRTH_CERTIFICATE, GOOD_CONDUCT:
                if (!DOC_EXT.contains(ext))
                    throw new IllegalArgumentException("Only PDF, DOC, DOCX allowed for " + kind);
                if (size > 10 * 1024 * 1024)
                    throw new IllegalArgumentException(kind + " must be under 10 MB");
                break;
            case VIDEO:
                if (!VIDEO_EXT.contains(ext))
                    throw new IllegalArgumentException("Only MP4, WebM, MOV allowed for " + kind);
                if (size > 50 * 1024 * 1024)
                    throw new IllegalArgumentException("Video must be under 50 MB");
                break;
        }
    }

    public MediaFile storeVideoLink(Long applicationId, String youtubeUrl) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NoSuchElementException("Application not found"));
        return saveVideoLink(youtubeUrl, app, null);
    }

    public MediaFile storeInterVideoLink(Long interApplicationId, String youtubeUrl) {
        InterApplication interApp = interApplicationRepository.findById(interApplicationId)
                .orElseThrow(() -> new NoSuchElementException("InterApplication not found"));
        return saveVideoLink(youtubeUrl, null, interApp);
    }

    private MediaFile saveVideoLink(String youtubeUrl, Application app, InterApplication interApp) {
        if (!youtubeUrl.startsWith("http") || !youtubeUrl.contains("youtube.com")) {
            throw new IllegalArgumentException("Invalid YouTube URL");
        }

        MediaFile media = MediaFile.builder()
                .fileName("YouTube Video")
                .fileType("video/link")
                .fileUrl(youtubeUrl)
                .kind(MediaFile.Kind.VIDEO)
                .application(app)
                .interApplication(interApp)
                .build();

        return mediaFileRepository.save(media);
    }

    public List<MediaFile> findByApplicationAndKind(Long applicationId, MediaFile.Kind kind) {
        return mediaFileRepository.findByApplicationIdAndKind(applicationId, kind);
    }

    public List<MediaFile> findByInterApplicationAndKind(Long interApplicationId, MediaFile.Kind kind) {
        return mediaFileRepository.findByInterApplicationIdAndKind(interApplicationId, kind);
    }

    public void delete(MediaFile mf) {
        mediaFileRepository.delete(mf);
        if (mf.getFileUrl() != null && !mf.getFileUrl().startsWith("http")) {
            fileStorageService.delete(mf.getFileUrl());
        }
    }
}
