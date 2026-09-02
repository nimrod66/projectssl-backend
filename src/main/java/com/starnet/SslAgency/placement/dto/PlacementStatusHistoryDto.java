package com.starnet.SslAgency.placement.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementStatusHistoryDto {
    private Long id;
    private String stage;
    private String note;
    private Long changedBy;
    private String changedByName;
    private LocalDateTime changedAt;
}
