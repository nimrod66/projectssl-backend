package com.starnet.SslAgency.placement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.starnet.SslAgency.processor.model.Staff;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity(name = "LegacyPlacementDocument")
@Table(name = "placement_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "placement_id", nullable = false)
    @JsonIgnore
    private Placement placement;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileUrl;

    private String fileType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocKind docKind;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private Staff uploadedBy;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    public enum DocKind {
        PASSPORT_COPY, VISA_COPY, TICKET, CONTRACT_SIGNED,
        MEDICAL_REPORT, POLICE_CLEARANCE, OTHER
    }
}
