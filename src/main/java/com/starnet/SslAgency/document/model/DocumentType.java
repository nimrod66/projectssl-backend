package com.starnet.SslAgency.document.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "document_types", indexes = {
        @Index(name = "idx_document_type_code", columnList = "code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "requires_verification", nullable = false)
    @Builder.Default
    private boolean requiresVerification = true;

    public enum Category {
        IDENTITY, CERTIFICATION, MEDICAL, EMPLOYMENT, TRAVEL
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;
}