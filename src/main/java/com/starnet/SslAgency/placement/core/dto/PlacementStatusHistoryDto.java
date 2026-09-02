package com.starnet.SslAgency.placement.core.dto;

import com.starnet.SslAgency.placement.core.model.Placement;
import com.starnet.SslAgency.placement.core.model.PlacementStatusHistory;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementStatusHistoryDto {

    private Long id;
    private Placement.Stage fromStage;
    private Placement.Stage toStage;
    private String reason;
    private String actorName;
    private LocalDateTime createdAt;

    public static PlacementStatusHistoryDto from(PlacementStatusHistory h) {
        return PlacementStatusHistoryDto.builder()
                .id(h.getId())
                .fromStage(h.getFromStage())
                .toStage(h.getToStage())
                .reason(h.getReason())
                .actorName(h.getActor() != null
                        ? h.getActor().getFirstName() + " " + h.getActor().getLastName() : null)
                .createdAt(h.getCreatedAt())
                .build();
    }
}