package com.starnet.SslAgency.employer.service;

import com.starnet.SslAgency.employer.dto.EmployerRequestDto;
import com.starnet.SslAgency.employer.dto.EmployerResponseDto;
import com.starnet.SslAgency.employer.model.Employer;
import com.starnet.SslAgency.employer.repository.EmployerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EmployerService {

    @Autowired
    private EmployerRepository employerRepository;

    public List<Employer> getAllEmployers(String status) {
        if (status != null && !status.isEmpty()) {
            return employerRepository.findByStatus(Employer.Status.valueOf(status.toUpperCase()));
        }
        return employerRepository.findAll();
    }

    public Employer getEmployer(Long id) {
        return employerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found"));
    }

    @Transactional
    public Employer createEmployer(EmployerRequestDto dto) {
        Employer employer = Employer.builder()
                .companyName(dto.getCompanyName())
                .country(dto.getCountry())
                .contactName(dto.getContactName())
                .contactEmail(dto.getContactEmail())
                .contactPhone(dto.getContactPhone())
                .address(dto.getAddress())
                .notes(dto.getNotes())
                .status(dto.getStatus() != null ? Employer.Status.valueOf(dto.getStatus().toUpperCase()) : Employer.Status.ACTIVE)
                .build();
        return employerRepository.save(employer);
    }

    @Transactional
    public Employer updateEmployer(Long id, EmployerRequestDto dto) {
        Employer employer = getEmployer(id);
        employer.setCompanyName(dto.getCompanyName());
        employer.setCountry(dto.getCountry());
        employer.setContactName(dto.getContactName());
        employer.setContactEmail(dto.getContactEmail());
        employer.setContactPhone(dto.getContactPhone());
        employer.setAddress(dto.getAddress());
        employer.setNotes(dto.getNotes());
        if (dto.getStatus() != null) {
            employer.setStatus(Employer.Status.valueOf(dto.getStatus().toUpperCase()));
        }
        return employerRepository.save(employer);
    }

    @Transactional
    public void deleteEmployer(Long id) {
        employerRepository.deleteById(id);
    }

    public EmployerResponseDto toResponseDto(Employer e) {
        return EmployerResponseDto.builder()
                .id(e.getId())
                .companyName(e.getCompanyName())
                .country(e.getCountry())
                .contactName(e.getContactName())
                .contactEmail(e.getContactEmail())
                .contactPhone(e.getContactPhone())
                .address(e.getAddress())
                .notes(e.getNotes())
                .status(e.getStatus().name())
                .createdAt(e.getCreatedAt() != null ? e.getCreatedAt().toString() : null)
                .updatedAt(e.getUpdatedAt() != null ? e.getUpdatedAt().toString() : null)
                .build();
    }
}
