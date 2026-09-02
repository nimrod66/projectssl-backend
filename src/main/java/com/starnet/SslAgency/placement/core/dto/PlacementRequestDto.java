package com.starnet.SslAgency.placement.core.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementRequestDto {

    @NotNull
    private Long applicationId;

    private LocalDate startDate;
    private LocalDate expectedEndDate;
}