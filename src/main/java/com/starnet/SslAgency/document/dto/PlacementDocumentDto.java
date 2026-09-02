package com.starnet.SslAgency.document.dto;

import com.starnet.SslAgency.document.model.PlacementDocument;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementDocumentDto {

    private Long id;
    private Long placementId;
    private DocumentTypeDto documentType;
    private String fileUrl;
    private String originalName;
    private PlacementDocument.Status status;
    private String verifiedByName;
    private LocalDateTime verifiedAt;
    private LocalDateTime uploadedAt;

    public static PlacementDocumentDto from(PlacementDocument d) {
        return PlacementDocumentDto.builder()
                .id(d.getId())
                .placementId(d.getPlacement().getId())
                .documentType(DocumentTypeDto.from(d.getDocumentType()))
                .fileUrl(d.getFileAsset().getFileUrl())
                .originalName(d.getFileAsset().getOriginalName())
                .status(d.getStatus())
                .verifiedByName(d.getVerifiedBy() != null
                        ? d.getVerifiedBy().getFirstName() + " " + d.getVerifiedBy().getLastName() : null)
                .verifiedAt(d.getVerifiedAt())
                .uploadedAt(d.getUploadedAt())
                .build();
    }
}