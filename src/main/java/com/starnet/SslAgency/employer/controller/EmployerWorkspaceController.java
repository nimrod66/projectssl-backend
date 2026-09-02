package com.starnet.SslAgency.employer.controller;

import com.starnet.SslAgency.contract.model.Contract;
import com.starnet.SslAgency.contract.repository.ContractRepository;
import com.starnet.SslAgency.employer.model.Employer;
import com.starnet.SslAgency.employer.repository.EmployerRepository;
import com.starnet.SslAgency.placement.model.Placement;
import com.starnet.SslAgency.placement.repository.PlacementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employers")
public class EmployerWorkspaceController {

    @Autowired private EmployerRepository employerRepository;
    @Autowired private ContractRepository contractRepository;
    @Autowired private PlacementRepository placementRepository;

    @GetMapping("/{id}/workspace")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public Map<String, Object> getWorkspace(@PathVariable Long id) {
        Employer e = employerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found"));

        List<Contract> allContracts = contractRepository.findByEmployerId(id);
        List<Placement> allPlacements = placementRepository.findAll().stream()
                .filter(p -> p.getContract() != null && p.getContract().getEmployer() != null && p.getContract().getEmployer().getId().equals(id))
                .toList();

        List<Contract> activeContracts = allContracts.stream().filter(c -> c.getStatus() != Contract.Status.CLOSED).toList();
        List<Contract> pastContracts = allContracts.stream().filter(c -> c.getStatus() == Contract.Status.CLOSED).toList();

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id", e.getId());
        resp.put("companyName", e.getCompanyName());
        resp.put("country", e.getCountry());
        resp.put("contactName", e.getContactName());
        resp.put("contactEmail", e.getContactEmail());
        resp.put("contactPhone", e.getContactPhone());
        resp.put("address", e.getAddress());
        resp.put("notes", e.getNotes());
        resp.put("status", e.getStatus().name());

        resp.put("stats", Map.of(
                "totalContracts", allContracts.size(),
                "activeContracts", activeContracts.size(),
                "pastContracts", pastContracts.size(),
                "totalPlacements", allPlacements.size(),
                "activePlacements", allPlacements.stream().filter(p -> p.getStage() != Placement.Stage.COMPLETED && p.getStage() != Placement.Stage.RETURNED && p.getStage() != Placement.Stage.TERMINATED).count(),
                "deployedPlacements", allPlacements.stream().filter(p -> p.getStage() == Placement.Stage.DEPLOYED).count()
        ));

        resp.put("activeContracts", activeContracts.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<String, Object>();
            m.put("id", c.getId()); m.put("jobCategory", c.getJobCategory()); m.put("country", c.getCountry());
            m.put("numberOfPositions", c.getNumberOfPositions()); m.put("filledPositions", c.getFilledPositions());
            m.put("salary", c.getSalary()); m.put("currency", c.getCurrency()); m.put("status", c.getStatus().name());
            m.put("startDate", c.getStartDate() != null ? c.getStartDate().toString() : null);
            m.put("endDate", c.getEndDate() != null ? c.getEndDate().toString() : null);
            return m;
        }).toList());

        resp.put("pastContracts", pastContracts.stream().map(c -> Map.of(
                "id", c.getId(), "jobCategory", c.getJobCategory(), "status", c.getStatus().name()
        )).toList());

        resp.put("recentPlacements", allPlacements.stream()
                .sorted(Comparator.comparing(Placement::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(p -> {
                    String name = getCandidateName(p);
                    Map<String, Object> m = new LinkedHashMap<String, Object>();
                    m.put("id", p.getId()); m.put("candidateName", name); m.put("stage", p.getStage().name());
                    m.put("startDate", p.getContractStartDate() != null ? p.getContractStartDate().toString() : null);
                    return m;
                }).toList());

        return resp;
    }

    private String getCandidateName(Placement p) {
        return "Candidate #" + (p.getApplicationId() != null ? p.getApplicationId() : p.getInterApplicationId());
    }
}
