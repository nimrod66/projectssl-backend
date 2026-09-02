package com.starnet.SslAgency.placement.core.dto;

import com.starnet.SslAgency.placement.core.model.PlacementChecklist;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementChecklistDto {

    private Long id;
    private PlacementChecklist.CheckItem item;
    private boolean required;
    private boolean completed;
    private LocalDateTime completedAt;
    private String completedByName;
    private String notes;

    public static PlacementChecklistDto from(PlacementChecklist c) {
        return PlacementChecklistDto.builder()
                .id(c.getId())
                .item(c.getItem())
                .required(c.isRequired())
                .completed(c.isCompleted())
                .completedAt(c.getCompletedAt())
                .completedByName(c.getCompletedBy() != null
                        ? c.getCompletedBy().getFirstName() + " " + c.getCompletedBy().getLastName() : null)
                .notes(c.getNotes())
                .build();
    }
}