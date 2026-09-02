package com.starnet.SslAgency.placement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlacementStageRequest {
    @NotBlank
    private String stage;
    private String note;
}
