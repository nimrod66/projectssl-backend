package com.starnet.SslAgency.report.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReport {
    private BigDecimal totalRevenue;
    private List<RevenueByEmployer> byEmployer;
    private List<RevenueByCountry> byCountry;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RevenueByEmployer {
        private String employerName;
        private long deployments;
        private BigDecimal revenue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RevenueByCountry {
        private String country;
        private long deployments;
        private BigDecimal revenue;
    }
}
