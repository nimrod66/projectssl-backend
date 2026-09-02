package com.starnet.SslAgency.contract.service;

import com.starnet.SslAgency.application.model.Application;
import com.starnet.SslAgency.application.repository.ApplicationRepository;
import com.starnet.SslAgency.contract.dto.AssignCandidateRequest;
import com.starnet.SslAgency.contract.dto.ContractRequestDto;
import com.starnet.SslAgency.contract.dto.ContractResponseDto;
import com.starnet.SslAgency.contract.model.Contract;
import com.starnet.SslAgency.contract.repository.ContractRepository;
import com.starnet.SslAgency.employer.model.Employer;
import com.starnet.SslAgency.employer.repository.EmployerRepository;
import com.starnet.SslAgency.interapplication.model.InterApplication;
import com.starnet.SslAgency.interapplication.repository.InterApplicationRepository;
import com.starnet.SslAgency.placement.model.Placement;
import com.starnet.SslAgency.placement.repository.PlacementRepository;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.processor.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContractService {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private InterApplicationRepository interApplicationRepository;

    @Autowired
    private PlacementRepository placementRepository;

    public List<Contract> getAllContracts(String status, Long employerId) {
        if (employerId != null && status != null) {
            return contractRepository.findByEmployerIdAndStatus(employerId, Contract.Status.valueOf(status.toUpperCase()));
        }
        if (employerId != null) {
            return contractRepository.findByEmployerId(employerId);
        }
        if (status != null && !status.isEmpty()) {
            return contractRepository.findByStatus(Contract.Status.valueOf(status.toUpperCase()));
        }
        return contractRepository.findAll();
    }

    public Contract getContract(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contract not found"));
    }

    @Transactional
    public Contract createContract(ContractRequestDto dto, Long staffId) {
        Employer employer = employerRepository.findById(dto.getEmployerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employer not found"));
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff not found"));

        Contract contract = Contract.builder()
                .employer(employer)
                .jobCategory(dto.getJobCategory())
                .country(dto.getCountry())
                .numberOfPositions(dto.getNumberOfPositions())
                .salary(dto.getSalary())
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "USD")
                .durationMonths(dto.getDurationMonths())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .renewable(dto.isRenewable())
                .notes(dto.getNotes())
                .status(Contract.Status.OPEN)
                .createdBy(staff)
                .build();
        return contractRepository.save(contract);
    }

    @Transactional
    public Contract updateContract(Long id, ContractRequestDto dto) {
        Contract contract = getContract(id);
        Employer employer = employerRepository.findById(dto.getEmployerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employer not found"));
        contract.setEmployer(employer);
        contract.setJobCategory(dto.getJobCategory());
        contract.setCountry(dto.getCountry());
        contract.setNumberOfPositions(dto.getNumberOfPositions());
        contract.setSalary(dto.getSalary());
        contract.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "USD");
        contract.setDurationMonths(dto.getDurationMonths());
        contract.setStartDate(dto.getStartDate());
        contract.setEndDate(dto.getEndDate());
        contract.setRenewable(dto.isRenewable());
        contract.setNotes(dto.getNotes());
        return contractRepository.save(contract);
    }

    @Transactional
    public Placement assignCandidate(Long contractId, AssignCandidateRequest request, Long staffId) {
        Contract contract = getContract(contractId);
        if (contract.getStatus() != Contract.Status.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can only assign candidates to open contracts");
        }
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff not found"));

        String type = request.getCandidateType().toUpperCase();
        String candidateName;

        if ("LOCAL".equals(type)) {
            Application app = applicationRepository.findById(request.getCandidateId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Applicant not found"));
            if (app.getStatus() != Application.Status.APPROVED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only approved applicants can be assigned");
            }
            candidateName = app.getFirstName() + " " + (app.getMiddleName() != null ? app.getMiddleName() + " " : "") + app.getLastName();
        } else if ("INTERNATIONAL".equals(type)) {
            InterApplication inter = interApplicationRepository.findById(request.getCandidateId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "International applicant not found"));
            if (inter.getStatus() != InterApplication.Status.APPROVED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only approved applicants can be assigned");
            }
            candidateName = inter.getFirstName() + " " + (inter.getMiddleName() != null ? inter.getMiddleName() + " " : "") + inter.getLastName();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid candidateType. Use LOCAL or INTERNATIONAL");
        }

        Placement placement = Placement.builder()
                .contract(contract)
                .applicationId("LOCAL".equals(type) ? request.getCandidateId() : null)
                .interApplicationId("INTERNATIONAL".equals(type) ? request.getCandidateId() : null)
                .candidateType(type)
                .stage(Placement.Stage.ASSIGNED)
                .salary(contract.getSalary())
                .currency(contract.getCurrency())
                .contractStartDate(contract.getStartDate())
                .contractEndDate(contract.getEndDate())
                .assignedBy(staff)
                .assignedAt(LocalDateTime.now())
                .build();
        placement = placementRepository.save(placement);

        contract.setFilledPositions(contract.getFilledPositions() + 1);
        int remaining = contract.getNumberOfPositions() - contract.getFilledPositions();
        if (remaining <= 0) {
            contract.setStatus(Contract.Status.FILLED);
        }
        contractRepository.save(contract);

        return placement;
    }

    @Transactional
    public void deleteContract(Long id) {
        contractRepository.deleteById(id);
    }

    public ContractResponseDto toResponseDto(Contract c) {
        return ContractResponseDto.builder()
                .id(c.getId())
                .employerId(c.getEmployer() != null ? c.getEmployer().getId() : null)
                .employerName(c.getEmployer() != null ? c.getEmployer().getCompanyName() : null)
                .jobCategory(c.getJobCategory())
                .country(c.getCountry())
                .numberOfPositions(c.getNumberOfPositions())
                .filledPositions(c.getFilledPositions())
                .salary(c.getSalary())
                .currency(c.getCurrency())
                .durationMonths(c.getDurationMonths())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .renewable(c.isRenewable())
                .notes(c.getNotes())
                .status(c.getStatus().name())
                .createdBy(c.getCreatedBy() != null ? c.getCreatedBy().getId() : null)
                .createdAt(c.getCreatedAt() != null ? c.getCreatedAt().toString() : null)
                .updatedAt(c.getUpdatedAt() != null ? c.getUpdatedAt().toString() : null)
                .build();
    }
}
