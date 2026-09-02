package com.starnet.SslAgency.placement.model;

import com.starnet.SslAgency.contract.model.Contract;
import com.starnet.SslAgency.processor.model.Staff;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "LegacyPlacement")
@Table(name = "placements")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Placement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "application_id")
    private Long applicationId;
    @Column(name = "inter_application_id")
    private Long interApplicationId;

    @Column(nullable = false)
    private String candidateType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Stage stage = Stage.ASSIGNED;

    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private BigDecimal salary;
    private String currency;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private Staff assignedBy;

    private LocalDateTime assignedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "placement", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlacementStatusHistory> history = new ArrayList<>();

    @OneToMany(mappedBy = "placement", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlacementDocument> documents = new ArrayList<>();

    public enum Stage {
        ASSIGNED, ACCEPTED,
        DOCUMENTS_SUBMITTED, DOCUMENTS_VERIFIED, MEDICAL_DONE,
        CONTRACT_SIGNED, VISA_APPLIED, VISA_APPROVED,
        FLIGHT_BOOKED, PRE_DEPARTURE, DEPARTED, DEPLOYED,
        RENEWED, COMPLETED, RETURNED,
        DECLINED, TERMINATED
    }
}
