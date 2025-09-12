package com.starnet.SslAgency.application.service;


import com.starnet.SslAgency.application.dto.ApplicationFilterDto;
import com.starnet.SslAgency.application.dto.ApplicationRequestDto;
import com.starnet.SslAgency.application.dto.ApplicationSearchDto;
import com.starnet.SslAgency.application.model.Application;
import com.starnet.SslAgency.application.repository.ApplicationRepository;
import com.starnet.SslAgency.media.model.MediaFile;
import com.starnet.SslAgency.media.service.MediaFileService;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.processor.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ApplicationService {
    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private MediaFileService mediaFileService;

    public Application createApplication(ApplicationRequestDto dto) {
        Application application = Application.builder()
                .firstName(dto.getFirstName())
                .middleName(dto.getMiddleName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .dob(dto.getDob())
                .nationality(dto.getNationality())
                .experience(dto.getExperience())
                .currentSalary(dto.getCurrentSalary())
                .currentProfession(dto.getCurrentProfession())
                .hasCat(dto.getHasCat() != null ? dto.getHasCat() : false)
                .hasDog(dto.getHasDog() != null ? dto.getHasDog() : false)
                .extraPay(dto.getExtraPay() != null ? dto.getExtraPay() : false)
                .liveOut(dto.getLiveOut() != null ? dto.getLiveOut() : false)
                .privateRoom(dto.getPrivateRoom() != null ? dto.getPrivateRoom() : false)
                .elderlyCare(dto.getElderlyCare() != null ? dto.getElderlyCare() : false)
                .specialNeeds(dto.getSpecialNeeds() != null ? dto.getSpecialNeeds() : false)
                .olderThan1(dto.getOlderThan1() != null ? dto.getOlderThan1() : false)
                .youngerThan1(dto.getYoungerThan1() != null ? dto.getYoungerThan1() : false)
                .currentLocation(dto.getCurrentLocation())
                .languages(dto.getLanguages() != null
                        ? dto.getLanguages().stream()
                        .map(lang -> Application.Languages.valueOf(lang.toUpperCase()))
                        .collect(Collectors.toSet())
                        : new HashSet<>()
                )

                .employmentStatus(dto.getEmploymentStatus() != null
                        ? Application.EmploymentStatus.valueOf(dto.getEmploymentStatus().toUpperCase())
                        : null
                )
                .jobInterest(dto.getJobInterest() != null
                        ? Application.JobInterest.valueOf(dto.getJobInterest().toUpperCase().replace(' ', '_'))
                        : null
                )

                .status(Application.Status.PENDING)
                .build();

        return applicationRepository.save(application);
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAll()
                .stream()
                .toList();
    }


    public List<ApplicationSearchDto> searchByName(String keyword) {
        return applicationRepository.search(keyword).stream()
                .map(this::mapToSearchDto)
                .collect(Collectors.toList());
    }

    private ApplicationSearchDto mapToSearchDto(Application application) {
        // build full name
        String fullName = Stream.of(application.getFirstName(), application.getMiddleName(), application.getLastName())
                .filter(s -> s != null && !s.isBlank())
                .reduce((s1, s2) -> s1 + " " + s2)
                .orElse("");

        String passportPhotoUrl = application.getMediaFiles().stream()
                .filter(m -> m.getKind() == MediaFile.Kind.PASSPORT)
                .map(MediaFile::getFileUrl)
                .findFirst()
                .orElse(null);

        return ApplicationSearchDto.builder()
                .id(application.getId())
                .fullName(fullName)
                .phoneNumber(application.getPhoneNumber())
                .age(application.getAge())
                .passportPhotoUrl(passportPhotoUrl)
                .build();
    }

    public List<Application> filterApplications(ApplicationFilterDto filter) {
        List<Application> allApps = applicationRepository.findByStatus(Application.Status.APPROVED);

        return allApps.stream()
                .filter(app -> matchesBoolean(app.getHasCat(), filter.getHasCat()))
                .filter(app -> matchesBoolean(app.getHasDog(), filter.getHasDog()))
                .filter(app -> matchesBoolean(app.getExtraPay(), filter.getExtraPay()))
                .filter(app -> matchesBoolean(app.getLiveOut(), filter.getLiveOut()))
                .filter(app -> matchesBoolean(app.getPrivateRoom(), filter.getPrivateRoom()))
                .filter(app -> matchesBoolean(app.getElderlyCare(), filter.getElderlyCare()))
                .filter(app -> matchesBoolean(app.getSpecialNeeds(), filter.getSpecialNeeds()))
                .filter(app -> matchesBoolean(app.getOlderThan1(), filter.getOlderThan1()))
                .filter(app -> matchesBoolean(app.getYoungerThan1(), filter.getYoungerThan1()))
                .filter(app -> matchesLocation(app.getCurrentLocation(), filter.getCurrentLocation()))
                .collect(Collectors.toList());
    }

    private boolean matchesBoolean(Boolean appValue, Boolean filterValue) {
        return filterValue == null || (appValue != null && appValue.equals(filterValue));
    }

    private boolean matchesLocation(String appLoc, String filterLoc) {
        return filterLoc == null
                || (appLoc != null && appLoc.trim().equalsIgnoreCase(filterLoc.trim()));
    }


    public Application getApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application History not found"));
    }

    public Application markVetted(Long appId, Long staffId) {
        Application app = getApplication(appId);
        Staff staff = staffRepository.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));
        app.setStatus(Application.Status.VETTED);
        app.setVettedBy(staff);
        app.setVettedAt(LocalDateTime.now());
        return applicationRepository.save(app);
    }

    public Application approve(Long applicationId, Long staffId) {
        Application app = applicationRepository.findById(applicationId)

                .orElseThrow(() -> new RuntimeException("Application not found"));

        Staff staff = staffRepository.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));

        List<MediaFile> showcase = mediaFileService.findByApplicationAndKind(applicationId, MediaFile.Kind.SHOWCASE_PHOTO);
        if (showcase.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one showcase photo is required before approval.");
        }

        List<MediaFile> videos = mediaFileService.findByApplicationAndKind(applicationId, MediaFile.Kind.VIDEO);
        if (videos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one video is required before approval.");
        }

        app.setStatus(Application.Status.APPROVED);
        app.setApprovedBy(staff);
        app.setApprovedAt(LocalDateTime.now());

        return applicationRepository.save(app);
    }


    public Application reject(Long appId, Long staffId) {
        Application app = markVetted(appId, staffId);
        app.setStatus(Application.Status.REJECTED);
        return applicationRepository.save(app);
    }

    public Application markHired(Long applicationId, Long staffId) {
        Application app = getApplication(applicationId);

        if (app.getStatus() != Application.Status.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only approved applicants can be marked as hired.");
        }

        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        app.setStatus(Application.Status.HIRED);
        app.setHiredBy(staff);
        app.setHiredAt(LocalDateTime.now());

        return applicationRepository.save(app);
    }


    public Application restoreToApproved(Long appId, Long staffId) {
        Application app = getApplication(appId);
        if (app.getStatus() != Application.Status.HIRED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only hired applicants can be restored.");
        }
        if (app.getApprovedBy() == null || app.getApprovedAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot restore because applicant was never approved before hiring.");
        }

        app.setStatus(Application.Status.APPROVED);

        return applicationRepository.save(app);
    }


    public Page<Application> listByStatus(String status, Pageable pageable) {
        Application.Status st = Application.Status.valueOf(status.toUpperCase());
        return applicationRepository.findByStatus(st, pageable);
    }

    public List<Application> getPublicApplications() {
        return applicationRepository.findByStatus(
                Application.Status.APPROVED,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

}
