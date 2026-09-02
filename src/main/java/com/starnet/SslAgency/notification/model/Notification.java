package com.starnet.SslAgency.notification.model;

import com.starnet.SslAgency.processor.model.Staff;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    @Enumerated(EnumType.STRING)
    private Type type;

    private Long entityId;
    private String entityType;

    @Column(name = "recipient_staff_id")
    private Long recipientStaffId;

    @Builder.Default
    @Column(name = "is_read")
    private boolean read = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Type {
        CONTRACT_EXPIRING, DOCUMENT_MISSING, PENDING_APPROVAL, PLACEMENT_ACTION, DEPLOYMENT, STATUS_CHANGE
    }
}
