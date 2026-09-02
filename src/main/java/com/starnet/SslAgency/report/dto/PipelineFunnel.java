package com.starnet.SslAgency.report.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipelineFunnel {
    private FunnelStage applicants;
    private PlacementFunnel placements;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FunnelStage {
        private long registered;
        private long screened;
        private long interviewed;
        private long vetted;
        private long approved;
        private long hired;
        private long rejected;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlacementFunnel {
        private long assigned;
        private long accepted;
        private long visaApplied;
        private long visaApproved;
        private long deployed;
        private long completed;
        private long terminated;
    }
}
