package com.starnet.SslAgency.report.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpiringContractsReport {
    private List<ExpiringItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExpiringItem {
        private Long placementId;
        private String candidateName;
        private String employerName;
        private String country;
        private LocalDate contractEndDate;
        private long daysRemaining;
    }
}
