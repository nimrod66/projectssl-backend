package com.starnet.SslAgency.placement.core.dto;

import com.starnet.SslAgency.placement.core.model.Placement;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementStageRequest {

    @NotNull
    private Placement.Stage stage;

    private String reason;
}