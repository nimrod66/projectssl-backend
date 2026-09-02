package com.starnet.SslAgency.opportunity.service;

import com.starnet.SslAgency.employer.model.Employer;
import com.starnet.SslAgency.employer.repository.EmployerRepository;
import com.starnet.SslAgency.contract.model.Contract;
import com.starnet.SslAgency.contract.repository.ContractRepository;
import com.starnet.SslAgency.opportunity.dto.OpportunityRequestDto;
import com.starnet.SslAgency.opportunity.dto.OpportunityResponseDto;
import com.starnet.SslAgency.opportunity.model.Opportunity;
import com.starnet.SslAgency.opportunity.repository.OpportunityRepository;
import com.starnet.SslAgency.processor.model.Staff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OpportunityService {

    private static final Map<Opportunity.Status, Set<Opportunity.Status>> ALLOWED_TRANSITIONS;

    static {
        ALLOWED_TRANSITIONS = new EnumMap<>(Opportunity.Status.class);
        ALLOWED_TRANSITIONS.put(Opportunity.Status.DRAFT,
                Set.of(Opportunity.Status.PENDING_APPROVAL, Opportunity.Status.CLOSED));
        ALLOWED_TRANSITIONS.put(Opportunity.Status.PENDING_APPROVAL,
                Set.of(Opportunity.Status.OPEN, Opportunity.Status.DRAFT, Opportunity.Status.CLOSED));
        ALLOWED_TRANSITIONS.put(Opportunity.Status.OPEN,
                Set.of(Opportunity.Status.PAUSED, Opportunity.Status.FILLED, Opportunity.Status.CLOSED));
        ALLOWED_TRANSITIONS.put(Opportunity.Status.PAUSED,
                Set.of(Opportunity.Status.OPEN, Opportunity.Status.CLOSED));
    }

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Transactional
    public OpportunityResponseDto create(OpportunityRequestDto dto, Staff createdBy) {
        Employer employer = employerRepository.findById(dto.getEmployerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found"));

        Opportunity opportunity = Opportunity.builder()
                .employer(employer)
                .contract(dto.getContractId() != null ? contractRepository.findById(dto.getContractId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contract not found")) : null)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .country(dto.getCountry())
                .location(dto.getLocation())
                .jobCategory(dto.getJobCategory())
                .numberOfPositions(dto.getNumberOfPositions() != null ? dto.getNumberOfPositions() : 1)
                .filledPositions(dto.getFilledPositions() != null ? dto.getFilledPositions() : 0)
                .salaryMinimum(dto.getSalaryMinimum())
                .salaryMaximum(dto.getSalaryMaximum())
                .currency(dto.getCurrency())
                .durationMonths(dto.getDurationMonths())
                .startDate(dto.getStartDate())
                .benefits(dto.getBenefits())
                .termsAndConditions(dto.getTermsAndConditions())
                .workingHours(dto.getWorkingHours())
                .accommodationProvided(dto.getAccommodationProvided() != null && dto.getAccommodationProvided())
                .transportProvided(dto.getTransportProvided() != null && dto.getTransportProvided())
                .requiredExperience(dto.getRequiredExperience())
                .requiredEducation(dto.getRequiredEducation())
                .requiredSkills(dto.getRequiredSkills())
                .requiredLanguages(dto.getRequiredLanguages())
                .minimumAge(dto.getMinimumAge())
                .maximumAge(dto.getMaximumAge())
                .genderRequirement(dto.getGenderRequirement())
                .applicationDeadline(dto.getApplicationDeadline())
                .status(Opportunity.Status.DRAFT)
                .createdBy(createdBy)
                .build();

        opportunity = opportunityRepository.save(opportunity);
        return OpportunityResponseDto.from(opportunity);
    }

    @Transactional
    public OpportunityResponseDto update(Long id, OpportunityRequestDto dto) {
        Opportunity opportunity = getEntity(id);
        if (opportunity.getStatus() == Opportunity.Status.FILLED
                || opportunity.getStatus() == Opportunity.Status.CLOSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot edit a " + opportunity.getStatus().name().toLowerCase() + " opportunity");
        }

        opportunity.setTitle(dto.getTitle());
        opportunity.setDescription(dto.getDescription());
        opportunity.setCountry(dto.getCountry());
        opportunity.setLocation(dto.getLocation());
        opportunity.setJobCategory(dto.getJobCategory());
        opportunity.setNumberOfPositions(dto.getNumberOfPositions() != null ? dto.getNumberOfPositions() : opportunity.getNumberOfPositions());
        opportunity.setSalaryMinimum(dto.getSalaryMinimum());
        opportunity.setSalaryMaximum(dto.getSalaryMaximum());
        opportunity.setCurrency(dto.getCurrency());
        opportunity.setDurationMonths(dto.getDurationMonths());
        opportunity.setStartDate(dto.getStartDate());
        opportunity.setBenefits(dto.getBenefits());
        opportunity.setTermsAndConditions(dto.getTermsAndConditions());
        opportunity.setWorkingHours(dto.getWorkingHours());
        if (dto.getAccommodationProvided() != null) {
            opportunity.setAccommodationProvided(dto.getAccommodationProvided());
        }
        if (dto.getTransportProvided() != null) {
            opportunity.setTransportProvided(dto.getTransportProvided());
        }
        opportunity.setRequiredExperience(dto.getRequiredExperience());
        opportunity.setRequiredEducation(dto.getRequiredEducation());
        opportunity.setRequiredSkills(dto.getRequiredSkills());
        opportunity.setRequiredLanguages(dto.getRequiredLanguages());
        opportunity.setMinimumAge(dto.getMinimumAge());
        opportunity.setMaximumAge(dto.getMaximumAge());
        opportunity.setGenderRequirement(dto.getGenderRequirement());
        opportunity.setApplicationDeadline(dto.getApplicationDeadline());
        if (dto.getEmployerId() != null && !dto.getEmployerId().equals(opportunity.getEmployer().getId())) {
            opportunity.setEmployer(employerRepository.findById(dto.getEmployerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found")));
        }

        opportunityRepository.save(opportunity);
        return OpportunityResponseDto.from(opportunity);
    }

    @Transactional
    public OpportunityResponseDto transition(Long id, Opportunity.Status target) {
        Opportunity opportunity = getEntity(id);
        Opportunity.Status current = opportunity.getStatus();

        if (!ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(target)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid opportunity status transition from " + current + " to " + target);
        }

        if (target == Opportunity.Status.FILLED) {
            opportunity.setFilledPositions(opportunity.getNumberOfPositions());
        }

        opportunity.setStatus(target);
        opportunityRepository.save(opportunity);
        return OpportunityResponseDto.from(opportunity);
    }

    public List<OpportunityResponseDto> listPublicOpen() {
        return opportunityRepository.findByStatusOrderByCreatedAtDesc(Opportunity.Status.OPEN).stream()
                .map(OpportunityResponseDto::from)
                .toList();
    }

    public OpportunityResponseDto getPublic(Long id) {
        Opportunity opportunity = getEntity(id);
        if (opportunity.getStatus() != Opportunity.Status.OPEN) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Opportunity not available");
        }
        return OpportunityResponseDto.from(opportunity);
    }

    public List<OpportunityResponseDto> listAll() {
        return opportunityRepository.findAll().stream()
                .map(OpportunityResponseDto::from)
                .toList();
    }

    public List<OpportunityResponseDto> listByStatus(Opportunity.Status status) {
        return opportunityRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(OpportunityResponseDto::from)
                .toList();
    }

    public OpportunityResponseDto get(Long id) {
        return OpportunityResponseDto.from(getEntity(id));
    }

    public Opportunity getEntity(Long id) {
        return opportunityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opportunity not found"));
    }
}
