package com.starnet.SslAgency.report.service;

import com.starnet.SslAgency.application.model.Application;
import com.starnet.SslAgency.application.repository.ApplicationRepository;
import com.starnet.SslAgency.common.CandidateNameResolver;
import com.starnet.SslAgency.contract.model.Contract;
import com.starnet.SslAgency.contract.repository.ContractRepository;
import com.starnet.SslAgency.employer.repository.EmployerRepository;
import com.starnet.SslAgency.interapplication.model.InterApplication;
import com.starnet.SslAgency.interapplication.repository.InterApplicationRepository;
import com.starnet.SslAgency.placement.model.Placement;
import com.starnet.SslAgency.placement.repository.PlacementRepository;
import com.starnet.SslAgency.report.dto.ExpiringContractsReport;
import com.starnet.SslAgency.report.dto.PipelineFunnel;
import com.starnet.SslAgency.report.dto.ReportSummary;
import com.starnet.SslAgency.report.dto.RevenueReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private PlacementRepository placementRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private InterApplicationRepository interApplicationRepository;

    @Autowired
    private CandidateNameResolver candidateNameResolver;

    public ReportSummary getSummary() {
        List<Contract> allContracts = contractRepository.findAll();
        List<Placement> allPlacements = placementRepository.findAll();

        long openContracts = allContracts.stream().filter(c -> c.getStatus() == Contract.Status.OPEN).count();
        long filledContracts = allContracts.stream().filter(c -> c.getStatus() == Contract.Status.FILLED).count();
        long activeDeployments = allPlacements.stream().filter(p -> p.getStage() == Placement.Stage.DEPLOYED).count();
        long completed = allPlacements.stream().filter(p -> p.getStage() == Placement.Stage.COMPLETED || p.getStage() == Placement.Stage.RETURNED).count();

        BigDecimal totalRevenue = allPlacements.stream()
                .filter(p -> p.getSalary() != null)
                .map(p -> p.getSalary().multiply(BigDecimal.valueOf(p.getContract() != null ? p.getContract().getDurationMonths() : 0)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> deploymentsByCountry = allPlacements.stream()
                .filter(p -> p.getStage() == Placement.Stage.DEPLOYED && p.getContract() != null)
                .collect(Collectors.groupingBy(p -> p.getContract().getCountry(), LinkedHashMap::new, Collectors.counting()));

        Map<String, Long> deploymentsByCategory = allPlacements.stream()
                .filter(p -> p.getStage() == Placement.Stage.DEPLOYED && p.getContract() != null)
                .collect(Collectors.groupingBy(p -> p.getContract().getJobCategory(), LinkedHashMap::new, Collectors.counting()));

        return ReportSummary.builder()
                .totalEmployers(employerRepository.count())
                .openContracts(openContracts)
                .filledContracts(filledContracts)
                .activeDeployments(activeDeployments)
                .completedPlacements(completed)
                .totalPlacements(allPlacements.size())
                .totalRevenue(totalRevenue)
                .deploymentsByCountry(deploymentsByCountry.entrySet().stream()
                        .map(e -> ReportSummary.DeploymentByCountry.builder().country(e.getKey()).count(e.getValue()).build())
                        .toList())
                .deploymentsByCategory(deploymentsByCategory.entrySet().stream()
                        .map(e -> ReportSummary.DeploymentByCategory.builder().category(e.getKey()).count(e.getValue()).build())
                        .toList())
                .build();
    }

    public ExpiringContractsReport getExpiring(int days) {
        LocalDate threshold = LocalDate.now().plusDays(days);
        List<Placement> deployed = placementRepository.findAll().stream()
                .filter(p -> p.getStage() == Placement.Stage.DEPLOYED && p.getContractEndDate() != null)
                .filter(p -> p.getContractEndDate().isBefore(threshold) || p.getContractEndDate().isEqual(threshold))
                .toList();

        List<ExpiringContractsReport.ExpiringItem> items = deployed.stream().map(p -> {
            long remaining = ChronoUnit.DAYS.between(LocalDate.now(), p.getContractEndDate());
            String employerName = p.getContract() != null && p.getContract().getEmployer() != null
                    ? p.getContract().getEmployer().getCompanyName() : "Unknown";
            return ExpiringContractsReport.ExpiringItem.builder()
                    .placementId(p.getId())
                    .candidateName(getCandidateName(p))
                    .employerName(employerName)
                    .country(p.getContract() != null ? p.getContract().getCountry() : null)
                    .contractEndDate(p.getContractEndDate())
                    .daysRemaining(Math.max(0, remaining))
                    .build();
        }).toList();

        return ExpiringContractsReport.builder().items(items).build();
    }

    public RevenueReport getRevenue() {
        List<Placement> all = placementRepository.findAll();

        BigDecimal total = all.stream()
                .filter(p -> p.getSalary() != null)
                .map(p -> p.getSalary().multiply(BigDecimal.valueOf(p.getContract() != null ? p.getContract().getDurationMonths() : 0)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, List<Placement>> byEmployer = all.stream()
                .filter(p -> p.getSalary() != null && p.getContract() != null && p.getContract().getEmployer() != null)
                .collect(Collectors.groupingBy(p -> p.getContract().getEmployer().getCompanyName()));

        List<RevenueReport.RevenueByEmployer> employers = byEmployer.entrySet().stream().map(e -> {
            BigDecimal rev = e.getValue().stream()
                    .map(p -> p.getSalary().multiply(BigDecimal.valueOf(p.getContract() != null ? p.getContract().getDurationMonths() : 0)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return RevenueReport.RevenueByEmployer.builder()
                    .employerName(e.getKey())
                    .deployments(e.getValue().size())
                    .revenue(rev)
                    .build();
        }).toList();

        Map<String, List<Placement>> byCountry = all.stream()
                .filter(p -> p.getSalary() != null && p.getContract() != null)
                .collect(Collectors.groupingBy(p -> p.getContract().getCountry()));

        List<RevenueReport.RevenueByCountry> countries = byCountry.entrySet().stream().map(e -> {
            BigDecimal rev = e.getValue().stream()
                    .map(p -> p.getSalary().multiply(BigDecimal.valueOf(p.getContract() != null ? p.getContract().getDurationMonths() : 0)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return RevenueReport.RevenueByCountry.builder()
                    .country(e.getKey())
                    .deployments(e.getValue().size())
                    .revenue(rev)
                    .build();
        }).toList();

        return RevenueReport.builder()
                .totalRevenue(total)
                .byEmployer(employers)
                .byCountry(countries)
                .build();
    }

    public PipelineFunnel getFunnel() {
        List<Application> locals = applicationRepository.findAll();
        List<InterApplication> internationals = interApplicationRepository.findAll();
        List<Placement> placements = placementRepository.findAll();

        long totalRegistered = locals.size() + internationals.size();
        long totalVetted = locals.stream().filter(a -> a.getStatus() == Application.Status.VETTED).count()
                + internationals.stream().filter(i -> i.getStatus() == InterApplication.Status.VETTED).count();
        long totalApproved = locals.stream().filter(a -> a.getStatus() == Application.Status.APPROVED).count()
                + internationals.stream().filter(i -> i.getStatus() == InterApplication.Status.APPROVED).count();
        long totalHired = locals.stream().filter(a -> a.getStatus() == Application.Status.HIRED).count()
                + internationals.stream().filter(i -> i.getStatus() == InterApplication.Status.HIRED).count();
        long totalRejected = locals.stream().filter(a -> a.getStatus() == Application.Status.REJECTED).count()
                + internationals.stream().filter(i -> i.getStatus() == InterApplication.Status.REJECTED).count();

        return PipelineFunnel.builder()
                .applicants(PipelineFunnel.FunnelStage.builder()
                        .registered(totalRegistered)
                        .vetted(totalVetted)
                        .approved(totalApproved)
                        .hired(totalHired)
                        .rejected(totalRejected)
                        .build())
                .placements(PipelineFunnel.PlacementFunnel.builder()
                        .assigned(placements.stream().filter(p -> p.getStage() == Placement.Stage.ASSIGNED || p.getStage() == Placement.Stage.ACCEPTED).count())
                        .visaApplied(placements.stream().filter(p -> p.getStage() == Placement.Stage.VISA_APPLIED).count())
                        .visaApproved(placements.stream().filter(p -> p.getStage() == Placement.Stage.VISA_APPROVED || p.getStage() == Placement.Stage.FLIGHT_BOOKED || p.getStage() == Placement.Stage.PRE_DEPARTURE || p.getStage() == Placement.Stage.DEPARTED).count())
                        .deployed(placements.stream().filter(p -> p.getStage() == Placement.Stage.DEPLOYED).count())
                        .completed(placements.stream().filter(p -> p.getStage() == Placement.Stage.COMPLETED || p.getStage() == Placement.Stage.RETURNED || p.getStage() == Placement.Stage.RENEWED).count())
                        .terminated(placements.stream().filter(p -> p.getStage() == Placement.Stage.TERMINATED || p.getStage() == Placement.Stage.DECLINED).count())
                        .build())
                .build();
    }

    private String getCandidateName(Placement p) {
        return candidateNameResolver.resolve(p);
    }
}
