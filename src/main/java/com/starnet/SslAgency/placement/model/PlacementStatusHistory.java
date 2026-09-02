package com.starnet.SslAgency.placement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.starnet.SslAgency.processor.model.Staff;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity(name = "LegacyPlacementStatusHistory")
@Table(name = "placement_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "placement_id", nullable = false)
    @JsonIgnore
    private Placement placement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Placement.Stage stage;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private Staff changedBy;

    @Column(nullable = false)
    private LocalDateTime changedAt;
}
