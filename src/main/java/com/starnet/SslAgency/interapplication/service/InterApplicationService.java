package com.starnet.SslAgency.interapplication.service;

import com.starnet.SslAgency.interapplication.dto.InterApplicationRequestDto;
import com.starnet.SslAgency.interapplication.model.InterApplication;
import com.starnet.SslAgency.interapplication.repository.InterApplicationRepository;
import com.starnet.SslAgency.media.service.MediaFileService;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.processor.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterApplicationService {
    @Autowired
    private InterApplicationRepository interApplicationRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private MediaFileService mediaFileService;

    public InterApplication createInterApplication(InterApplicationRequestDto dto) {
        InterApplication interApplication = InterApplication.builder()
                .firstName(dto.getFirstName())
                .middleName(dto.getMiddleName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .dob(dto.getDob())
                .nationality(dto.getNationality())
                .religion(dto.getReligion())
                .maritalStatus(dto.getMaritalStatus())
                .numberOfKids(dto.getNumberOfKids())
                .educationLevel(dto.getEducationLevel())
                .currentProfession(dto.getCurrentProfession())
                .currentSalary(dto.getCurrentSalary())
                .curentLocation(dto.getCurrentLocation())
                .languages(dto.getLanguages() != null
                        ? dto.getLanguages().stream()
                        .map(lang -> InterApplication.Languages.valueOf(lang.toUpperCase()))
                        .collect(Collectors.toSet())
                        : new HashSet<>()
                )
                .employmentStatus(dto.getEmploymentStatus() != null
                        ? InterApplication.EmploymentStatus.valueOf(dto.getEmploymentStatus().toUpperCase())
                        : null
                )
                .jobRecruitment(dto.getJobRecruitment() != null
                        ? InterApplication.JobRecruitment.valueOf(dto.getJobRecruitment().toUppperCase().replace(' ', '_'))
                        : null
                )

                .status(InterApplication.Status.PENDING)
                .build();

        return interApplicationRepository.save(interApplication);

    }

    public List<InterApplication> getAllInterApplications() {
        return interApplicationRepository.findAll()
                .stream()
                .toList();
    }

    public InterApplication getInterApplication(Long id) {
        return interApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application History not found"));
    }

    public InterApplication markVetted(Long interAppId, Long staffId){
        InterApplication interApp = getInterApplication(interAppId);
        Staff staff = staffRepository.findById(staffId).orElseThrow(()-> new RuntimeException("Staff not found"));
        interApp.setStatus(InterApplication.Status.VETTED);
        interApp.setVettedBy(staff);
        interApp.setVettedAt(LocalDateTime.now());
        return interApplicationRepository.save(interApp);
    }

}
