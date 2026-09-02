package com.starnet.SslAgency.document.dto;

import com.starnet.SslAgency.document.model.DocumentType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentTypeDto {

    private Long id;
    private String code;
    private String name;
    private String description;
    private boolean requiresVerification;
    private DocumentType.Category category;

    public static DocumentTypeDto from(DocumentType t) {
        return DocumentTypeDto.builder()
                .id(t.getId())
                .code(t.getCode())
                .name(t.getName())
                .description(t.getDescription())
                .requiresVerification(t.isRequiresVerification())
                .category(t.getCategory())
                .build();
    }
}