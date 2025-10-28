package com.starnet.SslAgency.interapplication.service;

import com.starnet.SslAgency.interapplication.dto.InterApplicationCVDto;
import com.starnet.SslAgency.interapplication.dto.InterApplicationRequestDto;
import com.starnet.SslAgency.interapplication.model.InterApplication;
import com.starnet.SslAgency.interapplication.repository.InterApplicationRepository;
import com.starnet.SslAgency.media.model.MediaFile;
import com.starnet.SslAgency.media.service.MediaFileService;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.processor.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public InterApplicationCVDto generateCVDto(Long id) {
        InterApplication interApp = interApplicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Applicant not found with ID: " + id));

        String fullName = String.format("%s %s %s",
                interApp.getFirstName() != null ? interApp.getFirstName() : "",
                interApp.getMiddleName() != null ? interApp.getMiddleName() : "",
                interApp.getLastName() != null ? interApp.getLastName() : "").trim();

        List<String> languageList = interApp.getLanguages() != null
                ? interApp.getLanguages().stream().map(Enum::name).toList()
                : List.of();

        return InterApplicationCVDto.builder()
                .id(interApp.getId())
                .fullName(fullName)
                .nationality(interApp.getNationality())
                .jobRecruitment(interApp.getJobRecruitment() != null ? interApp.getJobRecruitment().name() : null)
                .religion(interApp.getReligion())
                .currentProfession(interApp.getCurrentProfession())
                .currentSalary(interApp.getCurrentSalary())
                .dob(interApp.getDob())
                .age(interApp.getAge())
                .maritalStatus(interApp.getMaritalStatus() != null ? interApp.getMaritalStatus().name() : null)
                .numberOfKids(interApp.getNumberOfKids())
                .educationLevel(interApp.getEducationLevel() != null ? interApp.getEducationLevel().name() : null)
                .languages(languageList)
                .employmentStatus(interApp.getEmploymentStatus() != null ? interApp.getEmploymentStatus().name() : null)
                .build();
    }


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
                .maritalStatus(
                        dto.getMaritalStatus() != null
                                ? InterApplication.MaritalStatus.valueOf(dto.getMaritalStatus().toUpperCase())
                                : null

                )
                .numberOfKids(dto.getNumberOfKids())
                .educationLevel(
                        dto.getEducationLevel() != null
                                ? InterApplication.EducationLevel.valueOf(dto.getEducationLevel().toUpperCase())
                                : null

                )
                .currentProfession(dto.getCurrentProfession())
                .currentSalary(dto.getCurrentSalary())
                .currentLocation(dto.getCurrentLocation())
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
                        ? InterApplication.JobRecruitment.valueOf(dto.getJobRecruitment().toUpperCase().replace(' ', '_'))
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

    public List<InterApplication> getPublicInterApplications() {
        return interApplicationRepository.findByStatus(
                InterApplication.Status.APPROVED,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

    public InterApplication markVetted(Long interAppId, Long staffId) {
        InterApplication interApp = getInterApplication(interAppId);
        Staff staff = staffRepository.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));
        interApp.setStatus(InterApplication.Status.VETTED);
        interApp.setVettedBy(staff);
        interApp.setVettedAt(LocalDateTime.now());
        return interApplicationRepository.save(interApp);
    }

    public InterApplication approve(Long interAppId, Long staffId) {
        InterApplication interApp = interApplicationRepository.findById(interAppId)
                .orElseThrow(() -> new RuntimeException("Application not found in records"));

        Staff staff = staffRepository.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));

        List<MediaFile> showcase = mediaFileService.findByInterApplicationAndKind(interAppId, MediaFile.Kind.SHOWCASE_PHOTO);
        if (showcase.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one showcase photo is required");
        }

        List<MediaFile> videos = mediaFileService.findByInterApplicationAndKind(interAppId, MediaFile.Kind.VIDEO);
        if (videos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The youtube link is required for one to proceed");
        }

        interApp.setStatus(InterApplication.Status.APPROVED);
        interApp.setApprovedBy(staff);
        interApp.setApprovedAt(LocalDateTime.now());

        return interApplicationRepository.save(interApp);
    }

    public InterApplication reject(Long interAppId, Long staffId) {
        InterApplication interApp = markVetted(interAppId, staffId);
        interApp.setStatus(InterApplication.Status.REJECTED);
        return interApplicationRepository.save(interApp);
    }

    public InterApplication markHired(Long interAppId, Long staffId) {
        InterApplication interApp = getInterApplication(interAppId);

        if (interApp.getStatus() != InterApplication.Status.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only Approved Applicants can be marked as hired");
        }

        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        interApp.setStatus(InterApplication.Status.HIRED);
        interApp.setHiredBy(staff);
        interApp.setHiredAt(LocalDateTime.now());

        return interApplicationRepository.save(interApp);
    }

    public InterApplication restoreToApproved(Long interAppId, Long staffId) {
        InterApplication interApp = getInterApplication(interAppId);
        if (interApp.getStatus() != InterApplication.Status.HIRED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only hired applicants can be restored!");
        }
        if (interApp.getApprovedBy() == null || interApp.getApprovedAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot restore because applicant was never approved before hiring");
        }

        interApp.setStatus(InterApplication.Status.APPROVED);
        return interApplicationRepository.save(interApp);
    }

    public void deleteInternationalApplication(Long id) {
        interApplicationRepository.deleteById(id);
    }


}
