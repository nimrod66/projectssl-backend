package com.starnet.SslAgency.report.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSummary {
    private long totalEmployers;
    private long openContracts;
    private long filledContracts;
    private long activeDeployments;
    private long completedPlacements;
    private long totalPlacements;
    private BigDecimal totalRevenue;
    private List<DeploymentByCountry> deploymentsByCountry;
    private List<DeploymentByCategory> deploymentsByCategory;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeploymentByCountry {
        private String country;
        private long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeploymentByCategory {
        private String category;
        private long count;
    }
}
