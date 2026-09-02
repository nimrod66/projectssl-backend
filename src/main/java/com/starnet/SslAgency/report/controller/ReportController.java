package com.starnet.SslAgency.report.controller;

import com.starnet.SslAgency.report.dto.ExpiringContractsReport;
import com.starnet.SslAgency.report.dto.PipelineFunnel;
import com.starnet.SslAgency.report.dto.ReportSummary;
import com.starnet.SslAgency.report.dto.RevenueReport;
import com.starnet.SslAgency.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/summary")
    public ReportSummary getSummary() {
        return reportService.getSummary();
    }

    @GetMapping("/expiring")
    public ExpiringContractsReport getExpiring(@RequestParam(defaultValue = "60") int days) {
        return reportService.getExpiring(days);
    }

    @GetMapping("/revenue")
    public RevenueReport getRevenue() {
        return reportService.getRevenue();
    }

    @GetMapping("/funnel")
    public PipelineFunnel getFunnel() {
        return reportService.getFunnel();
    }
}
