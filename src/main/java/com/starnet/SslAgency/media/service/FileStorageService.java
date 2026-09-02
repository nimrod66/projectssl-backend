package com.starnet.SslAgency.media.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    /** Extensions we will ever persist. Anything else is rejected outright. */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".pdf", ".doc", ".docx",
            ".jpg", ".jpeg", ".png", ".webp", ".heic", ".heif", ".gif",
            ".mp4", ".mov", ".webm"
    );

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    public record StoredFile(String originalName, String contentType, String fileUrl, String storedName, long size) {}

    public StoredFile store(MultipartFile file) throws IOException {
        String original = file.getOriginalFilename() != null
                ? file.getOriginalFilename().replaceAll("\\s+", "_")
                : "file";
        String ext = getExtension(original);
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("File type not allowed: " + (ext.isEmpty() ? "(no extension)" : ext));
        }

        byte[] head = readHead(file);
        if (!matchesSignature(ext, head)) {
            throw new IllegalArgumentException("File content does not match its extension: " + ext);
        }

        Path uploadPath = resolveUploadDir();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String filename = UUID.randomUUID().toString().toLowerCase(Locale.ROOT) + ext;
        Path filePath = uploadPath.resolve(filename);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return new StoredFile(
                original,
                file.getContentType(),
                "/uploads/" + filename,
                filename,
                file.getSize()
        );
    }

    private byte[] readHead(MultipartFile file) throws IOException {
        byte[] buf = new byte[12];
        try (InputStream in = file.getInputStream()) {
            int read = in.read(buf);
            if (read <= 0) {
                throw new IllegalArgumentException("Empty file");
            }
            byte[] trimmed = new byte[read];
            System.arraycopy(buf, 0, trimmed, 0, read);
            return trimmed;
        }
    }

    private boolean matchesSignature(String ext, byte[] b) {
        return switch (ext) {
            case ".pdf" -> startsWith(b, new byte[]{0x25, 0x50, 0x44, 0x46});
            case ".jpg", ".jpeg" -> startsWith(b, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
            case ".png" -> startsWith(b, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
            case ".gif" -> startsWith(b, new byte[]{0x47, 0x49, 0x46, 0x38});
            case ".webp" -> b.length >= 12 && startsWith(b, "RIFF".getBytes()) && startsWith(b, "WEBP".getBytes(), 8);
            case ".mp4", ".mov" -> b.length >= 12 && startsWith(b, "ftyp".getBytes(), 4);
            case ".webm" -> startsWith(b, new byte[]{0x1A, 0x45, (byte) 0xDF, (byte) 0xA3});
            case ".doc" -> startsWith(b, new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1});
            case ".docx" -> startsWith(b, new byte[]{0x50, 0x4B, 0x03, 0x04});
            case ".heic", ".heif" -> b.length >= 12 && startsWith(b, "ftyp".getBytes(), 4);
            default -> false;
        };
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        return startsWith(data, prefix, 0);
    }

    private boolean startsWith(byte[] data, byte[] prefix, int offset) {
        if (data.length < offset + prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (data[offset + i] != prefix[i]) return false;
        }
        return true;
    }

    public InputStream read(String fileUrl) throws IOException {
        String filename = stripUploadsPrefix(fileUrl);
        Path filePath = resolveUploadDir().resolve(filename);
        return Files.newInputStream(filePath);
    }

    public void delete(String fileUrl) {
        try {
            String filename = stripUploadsPrefix(fileUrl);
            Path filePath = resolveUploadDir().resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
    }

    public boolean exists(String fileUrl) {
        String filename = stripUploadsPrefix(fileUrl);
        return Files.exists(resolveUploadDir().resolve(filename));
    }

    private Path resolveUploadDir() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private String stripUploadsPrefix(String fileUrl) {
        return fileUrl.replaceFirst("^/uploads/", "");
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf(".");
        return (dot != -1) ? filename.substring(dot).toLowerCase() : "";
    }
}
