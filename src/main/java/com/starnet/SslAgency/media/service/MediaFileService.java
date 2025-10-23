package com.starnet.SslAgency.media.service;

import com.starnet.SslAgency.application.model.Application;
import com.starnet.SslAgency.application.repository.ApplicationRepository;
import com.starnet.SslAgency.interapplication.model.InterApplication;
import com.starnet.SslAgency.interapplication.repository.InterApplicationRepository;
import com.starnet.SslAgency.media.model.MediaFile;
import com.starnet.SslAgency.media.repository.MediaFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class MediaFileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private InterApplicationRepository interApplicationRepository;


    public MediaFile store(Long applicationId, MultipartFile file, MediaFile.Kind kind) throws IOException {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        return saveFile(file, kind, app, null);
    }

    public MediaFile storeInter(Long interApplicationId, MultipartFile file, MediaFile.Kind kind) throws IOException {
        InterApplication interApp = interApplicationRepository.findById(interApplicationId)
                .orElseThrow(() -> new RuntimeException("International Application not found"));
        return saveFile(file, kind, null, interApp);
    }

    private MediaFile saveFile(MultipartFile file, MediaFile.Kind kind,
                               Application app, InterApplication interApp) throws IOException {

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        String original = Objects.requireNonNull(file.getOriginalFilename());
        String safeOriginal = original.replaceAll("\\s+", "_");

        String ext = "";
        int dotIndex = safeOriginal.lastIndexOf(".");
        if (dotIndex != -1) {
            ext = safeOriginal.substring(dotIndex).toLowerCase();
        }

        if ((kind == MediaFile.Kind.RESUME
                || kind == MediaFile.Kind.NATIONAL_ID
                || kind == MediaFile.Kind.BIRTH_CERTIFICATE
                || kind == MediaFile.Kind.GOOD_CONDUCT)
                && !(ext.equals(".pdf") || ext.equals(".doc") || ext.equals(".docx"))) {
            throw new IllegalArgumentException("Invalid format. Only PDF/DOC/DOCX allowed for " + kind);
        }

        String filename = UUID.randomUUID() + ext;
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        MediaFile media = MediaFile.builder()
                .fileName(safeOriginal)
                .fileType(file.getContentType())
                .fileUrl("/uploads/" + filename)
                .kind(kind)
                .application(app)
                .interApplication(interApp)
                .build();

        return mediaFileRepository.save(media);
    }

    public MediaFile storeVideoLink(Long applicationId, String youtubeUrl) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        return saveVideoLink(youtubeUrl, app, null);
    }

    public MediaFile storeInterVideoLink(Long interApplicationId, String youtubeUrl) {
        InterApplication interApp = interApplicationRepository.findById(interApplicationId)
                .orElseThrow(() -> new RuntimeException("InterApplication not found"));
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
        try {
            Path filePath = Paths.get(uploadDir).resolve(mf.getFileUrl().replace("/uploads/", ""));
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
    }
}
