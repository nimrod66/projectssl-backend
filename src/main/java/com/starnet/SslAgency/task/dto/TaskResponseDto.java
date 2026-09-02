package com.starnet.SslAgency.task.dto;

import com.starnet.SslAgency.task.model.Task;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponseDto {

    private Long id;
    private String title;
    private String description;
    private Task.Status status;
    private Task.Priority priority;
    private Long assignedToId;
    private String assignedToName;
    private Long relatedApplicantId;
    private Long relatedOpportunityId;
    private String entityType;
    private Long entityId;
    private LocalDate dueDate;
    private Long createdById;
    private String createdByName;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskResponseDto from(Task t) {
        return TaskResponseDto.builder()
                .id(t.getId())
                .title(t.getTitle())
                .description(t.getDescription())
                .status(t.getStatus())
                .priority(t.getPriority())
                .assignedToId(t.getAssignedTo() != null ? t.getAssignedTo().getId() : null)
                .assignedToName(t.getAssignedTo() != null
                        ? t.getAssignedTo().getFirstName() + " " + t.getAssignedTo().getLastName() : null)
                .relatedApplicantId(t.getRelatedApplicantId())
                .relatedOpportunityId(t.getRelatedOpportunityId())
                .entityType(t.getEntityType())
                .entityId(t.getEntityId())
                .dueDate(t.getDueDate())
                .createdById(t.getCreatedBy() != null ? t.getCreatedBy().getId() : null)
                .createdByName(t.getCreatedBy() != null
                        ? t.getCreatedBy().getFirstName() + " " + t.getCreatedBy().getLastName() : null)
                .completedAt(t.getCompletedAt())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}