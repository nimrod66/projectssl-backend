package com.starnet.SslAgency.common;

import com.starnet.SslAgency.application.repository.ApplicationRepository;
import com.starnet.SslAgency.interapplication.repository.InterApplicationRepository;
import com.starnet.SslAgency.placement.model.Placement;
import org.springframework.stereotype.Component;

/**
 * Resolves a human-readable candidate name for a placement record.
 * Centralizes what was previously duplicated across several services/controllers.
 */
@Component
public class CandidateNameResolver {

    private final ApplicationRepository applicationRepository;
    private final InterApplicationRepository interApplicationRepository;

    public CandidateNameResolver(ApplicationRepository applicationRepository,
                                 InterApplicationRepository interApplicationRepository) {
        this.applicationRepository = applicationRepository;
        this.interApplicationRepository = interApplicationRepository;
    }

    public String resolve(Placement p) {
        if (p.getApplicationId() != null) {
            return applicationRepository.findById(p.getApplicationId())
                    .map(a -> join(a.getFirstName(), a.getMiddleName(), a.getLastName()))
                    .orElse("Unknown");
        }
        if (p.getInterApplicationId() != null) {
            return interApplicationRepository.findById(p.getInterApplicationId())
                    .map(i -> join(i.getFirstName(), i.getMiddleName(), i.getLastName()))
                    .orElse("Unknown");
        }
        return "Unknown";
    }

    private String join(String first, String middle, String last) {
        StringBuilder sb = new StringBuilder();
        if (first != null && !first.isBlank()) sb.append(first.trim());
        if (middle != null && !middle.isBlank()) sb.append(' ').append(middle.trim());
        if (last != null && !last.isBlank()) sb.append(' ').append(last.trim());
        return sb.toString().trim();
    }
}
