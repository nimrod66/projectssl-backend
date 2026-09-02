package com.starnet.SslAgency.document.dto;

import com.starnet.SslAgency.document.model.ApplicantMedia;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicantMediaDto {

    private Long id;
    private Long applicantId;
    private ApplicantMedia.MediaType mediaType;
    private String fileUrl;
    private String originalName;
    private String description;
    private LocalDateTime uploadedAt;

    public static ApplicantMediaDto from(ApplicantMedia m) {
        return ApplicantMediaDto.builder()
                .id(m.getId())
                .applicantId(m.getApplicant().getId())
                .mediaType(m.getMediaType())
                .fileUrl(m.getFileAsset().getFileUrl())
                .originalName(m.getFileAsset().getOriginalName())
                .description(m.getDescription())
                .uploadedAt(m.getUploadedAt())
                .build();
    }
}