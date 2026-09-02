package com.starnet.SslAgency.placement.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementDocumentResponseDto {
    private Long id;
    private Long placementId;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private String docKind;
    private Long uploadedBy;
    private String uploadedByName;
    private String uploadedAt;
}
