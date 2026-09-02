package com.starnet.SslAgency.dashboard.controller;

import com.starnet.SslAgency.application.model.Application;
import com.starnet.SslAgency.application.repository.ApplicationRepository;
import com.starnet.SslAgency.common.CandidateNameResolver;
import com.starnet.SslAgency.contract.model.Contract;
import com.starnet.SslAgency.contract.repository.ContractRepository;
import com.starnet.SslAgency.interapplication.model.InterApplication;
import com.starnet.SslAgency.interapplication.repository.InterApplicationRepository;
import com.starnet.SslAgency.placement.model.Placement;
import com.starnet.SslAgency.placement.repository.PlacementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private InterApplicationRepository interApplicationRepository;

    @Autowired
    private PlacementRepository placementRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private CandidateNameResolver candidateNameResolver;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public Map<String, Object> getDashboard() {
        List<Application> locals = applicationRepository.findAll();
        List<InterApplication> intls = interApplicationRepository.findAll();
        List<Placement> placements = placementRepository.findAll();
        List<Contract> contracts = contractRepository.findAll();

        long pendingReview = locals.stream().filter(a -> a.getStatus() == Application.Status.PENDING).count()
                + intls.stream().filter(i -> i.getStatus() == InterApplication.Status.PENDING).count();

        long vettedCount = locals.stream().filter(a -> a.getStatus() == Application.Status.VETTED).count()
                + intls.stream().filter(i -> i.getStatus() == InterApplication.Status.VETTED).count();

        long approvedCount = locals.stream().filter(a -> a.getStatus() == Application.Status.APPROVED).count()
                + intls.stream().filter(i -> i.getStatus() == InterApplication.Status.APPROVED).count();

        long hiredCount = locals.stream().filter(a -> a.getStatus() == Application.Status.HIRED).count()
                + intls.stream().filter(i -> i.getStatus() == InterApplication.Status.HIRED).count();

        long openContracts = contracts.stream().filter(c -> c.getStatus() == Contract.Status.OPEN).count();
        long filledContracts = contracts.stream().filter(c -> c.getStatus() == Contract.Status.FILLED).count();
        long activePlacements = placements.stream().filter(p -> p.getStage() == Placement.Stage.DEPLOYED).count();

        List<Map<String, Object>> recentRegistrations = Stream.concat(
                locals.stream().sorted(Comparator.comparing(Application::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))).limit(5),
                intls.stream().sorted(Comparator.comparing(InterApplication::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))).limit(5)
        ).sorted(Comparator.comparing((Object o) -> {
            if (o instanceof Application a) return a.getCreatedAt();
            if (o instanceof InterApplication i) return i.getCreatedAt();
            return LocalDateTime.now();
        }, Comparator.nullsLast(Comparator.reverseOrder())))
        .limit(8)
        .map(o -> {
            Map<String, Object> m = new LinkedHashMap<>();
            if (o instanceof Application a) {
                m.put("id", a.getId());
                m.put("fullName", a.getFirstName() + " " + (a.getMiddleName() != null ? a.getMiddleName() + " " : "") + a.getLastName());
                m.put("type", "local");
                m.put("status", a.getStatus().name());
                m.put("createdAt", a.getCreatedAt() != null ? a.getCreatedAt().toString() : null);
            } else if (o instanceof InterApplication i) {
                m.put("id", i.getId());
                m.put("fullName", i.getFirstName() + " " + (i.getMiddleName() != null ? i.getMiddleName() + " " : "") + i.getLastName());
                m.put("type", "international");
                m.put("status", i.getStatus().name());
                m.put("createdAt", i.getCreatedAt() != null ? i.getCreatedAt().toString() : null);
            }
            return m;
        }).toList();

        List<Map<String, Object>> placementsInProgress = placements.stream()
                .filter(p -> p.getStage() != Placement.Stage.COMPLETED && p.getStage() != Placement.Stage.RETURNED && p.getStage() != Placement.Stage.TERMINATED && p.getStage() != Placement.Stage.DECLINED)
                .sorted(Comparator.comparing(Placement::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getId());
                    m.put("candidateName", getCandidateName(p));
                    m.put("stage", p.getStage().name());
                    m.put("contractId", p.getContract() != null ? p.getContract().getId() : null);
                    m.put("employerName", p.getContract() != null && p.getContract().getEmployer() != null ? p.getContract().getEmployer().getCompanyName() : "N/A");
                    return m;
                }).toList();

        List<Map<String, Object>> contractsNearExpiry = placements.stream()
                .filter(p -> p.getStage() == Placement.Stage.DEPLOYED && p.getContractEndDate() != null)
                .filter(p -> {
                    long days = ChronoUnit.DAYS.between(LocalDate.now(), p.getContractEndDate());
                    return days >= 0 && days <= 90;
                })
                .sorted(Comparator.comparing(Placement::getContractEndDate))
                .limit(6)
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getId());
                    m.put("candidateName", getCandidateName(p));
                    m.put("employerName", p.getContract() != null && p.getContract().getEmployer() != null ? p.getContract().getEmployer().getCompanyName() : "N/A");
                    m.put("endDate", p.getContractEndDate() != null ? p.getContractEndDate().toString() : null);
                    m.put("daysLeft", p.getContractEndDate() != null ? Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), p.getContractEndDate())) : 0);
                    return m;
                }).toList();

        List<Map<String, Object>> recentDeployments = placements.stream()
                .filter(p -> p.getStage() == Placement.Stage.DEPLOYED)
                .sorted(Comparator.comparing(Placement::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getId());
                    m.put("candidateName", getCandidateName(p));
                    m.put("country", p.getContract() != null ? p.getContract().getCountry() : "N/A");
                    m.put("updatedAt", p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : null);
                    return m;
                }).toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("pendingReview", pendingReview);
        response.put("quickStats", Map.of(
                "totalCandidates", locals.size() + intls.size(),
                "pendingReview", pendingReview,
                "vetted", vettedCount,
                "approved", approvedCount,
                "hired", hiredCount,
                "activePlacements", activePlacements,
                "openContracts", openContracts,
                "filledContracts", filledContracts
        ));
        response.put("recentRegistrations", recentRegistrations);
        response.put("placementsInProgress", placementsInProgress);
        response.put("contractsNearExpiry", contractsNearExpiry);
        response.put("recentDeployments", recentDeployments);

        return response;
    }

    private String getCandidateName(Placement p) {
        return candidateNameResolver.resolve(p);
    }
}
