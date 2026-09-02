package com.starnet.SslAgency.search.controller;

import com.starnet.SslAgency.application.model.Application;
import com.starnet.SslAgency.application.repository.ApplicationRepository;
import com.starnet.SslAgency.contract.model.Contract;
import com.starnet.SslAgency.contract.repository.ContractRepository;
import com.starnet.SslAgency.employer.model.Employer;
import com.starnet.SslAgency.employer.repository.EmployerRepository;
import com.starnet.SslAgency.interapplication.model.InterApplication;
import com.starnet.SslAgency.interapplication.repository.InterApplicationRepository;
import com.starnet.SslAgency.placement.model.Placement;
import com.starnet.SslAgency.placement.repository.PlacementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private InterApplicationRepository interApplicationRepository;
    @Autowired private EmployerRepository employerRepository;
    @Autowired private ContractRepository contractRepository;
    @Autowired private PlacementRepository placementRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public Map<String, Object> search(@RequestParam String q) {
        String query = q.toLowerCase().trim();
        List<Map<String, Object>> candidates = new ArrayList<>();
        List<Map<String, Object>> employers = new ArrayList<>();
        List<Map<String, Object>> contracts = new ArrayList<>();

        applicationRepository.findAll().stream()
                .filter(a -> matches(query, a.getFirstName(), a.getMiddleName(), a.getLastName(), a.getEmail(), a.getPhoneNumber(), a.getNationality()))
                .limit(5)
                .forEach(a -> candidates.add(Map.of(
                        "id", a.getId(), "fullName", a.getFirstName() + " " + a.getLastName(),
                        "type", "local", "status", a.getStatus().name(), "entityType", "candidate"
                )));

        interApplicationRepository.findAll().stream()
                .filter(i -> matches(query, i.getFirstName(), i.getMiddleName(), i.getLastName(), i.getEmail(), i.getPhoneNumber(), i.getNationality()))
                .limit(5)
                .forEach(i -> candidates.add(Map.of(
                        "id", i.getId(), "fullName", i.getFirstName() + " " + i.getLastName(),
                        "type", "international", "status", i.getStatus().name(), "entityType", "candidate"
                )));

        employerRepository.findAll().stream()
                .filter(e -> matches(query, e.getCompanyName(), e.getCountry(), e.getContactName(), e.getContactEmail()))
                .limit(5)
                .forEach(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", e.getId()); m.put("name", e.getCompanyName()); m.put("country", e.getCountry());
                    m.put("entityType", "employer");
                    employers.add(m);
                });

        contractRepository.findAll().stream()
                .filter(c -> matches(query, c.getJobCategory(), c.getCountry())
                        || (c.getEmployer() != null && matches(query, c.getEmployer().getCompanyName())))
                .limit(5)
                .forEach(c -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", c.getId());
                    m.put("name", (c.getEmployer() != null ? c.getEmployer().getCompanyName() + " - " : "") + c.getJobCategory());
                    m.put("country", c.getCountry()); m.put("entityType", "contract");
                    contracts.add(m);
                });

        return Map.of("candidates", candidates, "employers", employers, "contracts", contracts);
    }

    private boolean matches(String query, String... fields) {
        for (String f : fields) {
            if (f != null && f.toLowerCase().contains(query)) return true;
        }
        return false;
    }
}
