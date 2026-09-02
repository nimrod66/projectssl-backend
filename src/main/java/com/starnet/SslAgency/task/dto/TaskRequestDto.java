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
public class TaskRequestDto {

    private String title;
    private String description;
    private Task.Priority priority;
    private Long assignedToId;
    private Long relatedApplicantId;
    private Long relatedOpportunityId;
    private LocalDate dueDate;
}