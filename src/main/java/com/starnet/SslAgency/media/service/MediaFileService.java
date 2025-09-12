package com.starnet.SslAgency.media.service;

import com.starnet.SslAgency.application.model.Application;
import com.starnet.SslAgency.application.repository.ApplicationRepository;
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

    public MediaFile store(Long applicationId, MultipartFile file, MediaFile.Kind kind) throws IOException {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        String original = Objects.requireNonNull(file.getOriginalFilename());
        String safeOriginal = original.replaceAll("\\s+", "_");

        String ext = "";
        int dotIndex = safeOriginal.lastIndexOf(".");
        if (dotIndex != -1) {
            ext = safeOriginal.substring(dotIndex).toLowerCase();
        }

        String filename = UUID.randomUUID() + ext;
        Path filePath = uploadPath.resolve(filename);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        if ((kind == MediaFile.Kind.RESUME
                || kind == MediaFile.Kind.BIRTH_CERTIFICATE
                || kind == MediaFile.Kind.GOOD_CONDUCT)
                && !(ext.equals(".pdf") || ext.equals(".doc") || ext.equals(".docx"))) {
            throw new IllegalArgumentException("Invalid format. Only PDF/DOC/DOCX allowed " + kind);
        }

        MediaFile media = MediaFile.builder()
                .fileName(safeOriginal)
                .fileType(file.getContentType())
                .fileUrl("/uploads/" + filename)
                .kind(kind)
                .application(app)
                .build();

        return mediaFileRepository.save(media);
    }

    public List<MediaFile> findByApplicationAndKind(Long applicationId, MediaFile.Kind kind) {
        return mediaFileRepository.findByApplicationIdAndKind(applicationId, kind);
    }

    public void delete(MediaFile mf) {
        mediaFileRepository.delete(mf);
        try {
            Files.deleteIfExists(Paths.get(uploadDir).resolve(mf.getFileUrl().replace("/uploads/", "")));

        } catch (IOException ignored) {
        }
    }
}


