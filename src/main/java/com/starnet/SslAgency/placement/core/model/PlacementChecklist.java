package com.starnet.SslAgency.placement.core.model;

import com.starnet.SslAgency.processor.model.Staff;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "placement_checklist", indexes = {
        @Index(name = "idx_pc_placement", columnList = "placement_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "placement_id", nullable = false)
    @ToString.Exclude
    private Placement placement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CheckItem item;

    @Column(nullable = false)
    @Builder.Default
    private boolean required = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by")
    private Staff completedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum CheckItem {
        PASSPORT,
        MEDICAL,
        VISA,
        SIGNED_CONTRACT,
        FLIGHT_BOOKING,
        EMPLOYER_CONFIRMATION,
        CANDIDATE_BRIEFING,
        EMERGENCY_CONTACT
    }
}