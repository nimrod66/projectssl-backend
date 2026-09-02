package com.starnet.SslAgency.applicant.service;

import com.starnet.SslAgency.applicant.dto.ApplicantProfileDto;
import com.starnet.SslAgency.applicant.dto.ApplicantRequestDto;
import com.starnet.SslAgency.applicant.dto.ApplicantResponseDto;
import com.starnet.SslAgency.applicant.model.Applicant;
import com.starnet.SslAgency.applicant.model.ApplicantProfile;
import com.starnet.SslAgency.applicant.model.ApplicantTimeline;
import com.starnet.SslAgency.applicant.repository.ApplicantProfileRepository;
import com.starnet.SslAgency.applicant.repository.ApplicantRepository;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.processor.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Year;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ApplicantService {

    private static final Map<Applicant.LifecycleStage, Set<Applicant.LifecycleStage>> ALLOWED_TRANSITIONS;

    static {
        ALLOWED_TRANSITIONS = new EnumMap<>(Applicant.LifecycleStage.class);
        ALLOWED_TRANSITIONS.put(Applicant.LifecycleStage.REGISTERED,
                Set.of(Applicant.LifecycleStage.PROFILE_COMPLETE));
        ALLOWED_TRANSITIONS.put(Applicant.LifecycleStage.PROFILE_COMPLETE,
                Set.of(Applicant.LifecycleStage.UNDER_REVIEW, Applicant.LifecycleStage.INACTIVE));
        ALLOWED_TRANSITIONS.put(Applicant.LifecycleStage.UNDER_REVIEW,
                Set.of(Applicant.LifecycleStage.VETTED, Applicant.LifecycleStage.INACTIVE));
        ALLOWED_TRANSITIONS.put(Applicant.LifecycleStage.VETTED,
                Set.of(Applicant.LifecycleStage.ELIGIBLE, Applicant.LifecycleStage.INACTIVE));
        ALLOWED_TRANSITIONS.put(Applicant.LifecycleStage.ELIGIBLE,
                Set.of(Applicant.LifecycleStage.INACTIVE));
        ALLOWED_TRANSITIONS.put(Applicant.LifecycleStage.INACTIVE,
                Set.of(Applicant.LifecycleStage.PROFILE_COMPLETE, Applicant.LifecycleStage.UNDER_REVIEW,
                        Applicant.LifecycleStage.VETTED, Applicant.LifecycleStage.ELIGIBLE));
    }

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private ApplicantProfileRepository profileRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicantTimelineService timelineService;

    @Transactional
    public ApplicantResponseDto register(ApplicantRequestDto dto) {
        String phone = dto.getPhoneNumber() != null ? dto.getPhoneNumber().trim() : "";
        if (applicantRepository.existsByPhoneNumber(phone)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number already registered");
        }

        Applicant applicant = Applicant.builder()
                .applicantNumber("P-" + java.util.UUID.randomUUID().toString().substring(0, 8))
                .firstName(dto.getFirstName())
                .middleName(dto.getMiddleName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phoneNumber(phone)
                .alternativePhone(dto.getAlternativePhone())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .nationality(dto.getNationality())
                .county(dto.getCounty())
                .address(dto.getAddress())
                .registrationSource(dto.getRegistrationSource() != null
                        ? dto.getRegistrationSource() : Applicant.RegistrationSource.WEBSITE)
                .applicantType(dto.getApplicantType() != null
                        ? dto.getApplicantType() : Applicant.ApplicantType.LOCAL)
                .lifecycleStage(Applicant.LifecycleStage.REGISTERED)
                .status(Applicant.Status.ACTIVE)
                .passwordHash(dto.getPassword() != null && !dto.getPassword().isBlank()
                        ? passwordEncoder.encode(dto.getPassword()) : null)
                .build();

        applicant = applicantRepository.save(applicant);
        applicant.setApplicantNumber("SSL-" + Year.now().getValue() + "-" + String.format("%05d", applicant.getId()));
        applicant = applicantRepository.save(applicant);

        timelineService.log(applicant, ApplicantTimeline.EventType.REGISTERED,
                "Applicant registered via " + applicant.getRegistrationSource().name(), null,
                "type=" + applicant.getApplicantType().name());

        return ApplicantResponseDto.from(applicant);
    }

    @Transactional
    public ApplicantResponseDto updateProfile(Long applicantId, ApplicantProfileDto dto) {
        Applicant applicant = getEntity(applicantId);

        ApplicantProfile profile = applicant.getProfile();
        if (profile == null) {
            profile = ApplicantProfile.builder().applicant(applicant).build();
            applicant.attachProfile(profile);
        }
        profile.setEducationLevel(dto.getEducationLevel());
        profile.setFieldOfStudy(dto.getFieldOfStudy());
        profile.setProfessionalSummary(dto.getProfessionalSummary());
        profile.setYearsOfExperience(dto.getYearsOfExperience());
        profile.setSkills(dto.getSkills());
        profile.setLanguages(dto.getLanguages());
        profile.setPreferredJobCategories(dto.getPreferredJobCategories());
        profile.setPreferredCountries(dto.getPreferredCountries());
        profile.setPreferredSalary(dto.getPreferredSalary());
        profile.setPreferredSalaryCurrency(dto.getPreferredSalaryCurrency());
        profile.setAvailability(dto.getAvailability());
        profile.setAvailableFrom(dto.getAvailableFrom());
        if (dto.getWillingToRelocate() != null) {
            profile.setWillingToRelocate(dto.getWillingToRelocate());
        }
        profile.setEmploymentStatus(dto.getEmploymentStatus());
        profile.setCurrentEmployer(dto.getCurrentEmployer());
        profile.setCurrentPosition(dto.getCurrentPosition());
        profile.setRelevantExperience(dto.getRelevantExperience());
        profile.setReasonForLeaving(dto.getReasonForLeaving());
        profile.setReligion(dto.getReligion());
        profile.setMaritalStatus(dto.getMaritalStatus());
        profile.setNumberOfChildren(dto.getNumberOfChildren());
        profile.setNextOfKinName(dto.getNextOfKinName());
        profile.setNextOfKinPhone(dto.getNextOfKinPhone());
        profile.setNextOfKinRelationship(dto.getNextOfKinRelationship());

        profileRepository.save(profile);

        boolean coreComplete = applicant.getFirstName() != null && !applicant.getFirstName().isBlank()
                && applicant.getLastName() != null && !applicant.getLastName().isBlank()
                && applicant.getPhoneNumber() != null && !applicant.getPhoneNumber().isBlank();
        boolean profileComplete = profile.getEducationLevel() != null
                && profile.getYearsOfExperience() != null
                && profile.getAvailability() != null;

        if (coreComplete && profileComplete
                && applicant.getLifecycleStage() == Applicant.LifecycleStage.REGISTERED) {
            applicant.setLifecycleStage(Applicant.LifecycleStage.PROFILE_COMPLETE);
            timelineService.log(applicant, ApplicantTimeline.EventType.PROFILE_COMPLETED,
                    "Profile completed and marked ready for review", null, null);
        } else {
            timelineService.log(applicant, ApplicantTimeline.EventType.PROFILE_UPDATED,
                    "Profile updated", null, null);
        }

        applicantRepository.save(applicant);
        return ApplicantResponseDto.from(applicant);
    }

    @Transactional
    public ApplicantResponseDto assignRecruiter(Long applicantId, Long recruiterId) {
        Applicant applicant = getEntity(applicantId);
        Staff recruiter = staffRepository.findById(recruiterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recruiter not found"));
        applicant.setAssignedRecruiter(recruiter);
        applicantRepository.save(applicant);
        timelineService.log(applicant, ApplicantTimeline.EventType.RECRUITER_ASSIGNED,
                "Recruiter assigned: " + recruiter.getFirstName() + " " + recruiter.getLastName(),
                recruiter, null);
        return ApplicantResponseDto.from(applicant);
    }

    @Transactional
    public ApplicantResponseDto transition(Long applicantId, Applicant.LifecycleStage target, String reason, Staff actor) {
        Applicant applicant = getEntity(applicantId);
        Applicant.LifecycleStage current = applicant.getLifecycleStage();

        if (target == Applicant.LifecycleStage.BLACKLISTED) {
            applicant.setStatus(Applicant.Status.BLACKLISTED);
            applicant.setLifecycleStage(Applicant.LifecycleStage.BLACKLISTED);
            applicantRepository.save(applicant);
            timelineService.log(applicant, ApplicantTimeline.EventType.BLACKLISTED,
                    "Applicant blacklisted" + (reason != null ? ": " + reason : ""), actor, null);
            return ApplicantResponseDto.from(applicant);
        }

        if (!ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(target)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid lifecycle transition from " + current + " to " + target);
        }

        if (target == Applicant.LifecycleStage.INACTIVE) {
            applicant.setStatus(Applicant.Status.INACTIVE);
        } else if (applicant.getStatus() != Applicant.Status.ACTIVE && applicant.getStatus() != Applicant.Status.INACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Applicant is not active");
        } else {
            applicant.setStatus(Applicant.Status.ACTIVE);
        }

        applicant.setLifecycleStage(target);
        applicantRepository.save(applicant);

        timelineService.log(applicant, ApplicantTimeline.EventType.LIFE_STAGE_CHANGED,
                "Lifecycle: " + current + " -> " + target + (reason != null ? " (" + reason + ")" : ""),
                actor, "from=" + current + ";to=" + target);

        return ApplicantResponseDto.from(applicant);
    }

    public ApplicantResponseDto get(Long id) {
        return ApplicantResponseDto.from(getEntity(id));
    }

    public ApplicantResponseDto getByPhone(String phone) {
        Applicant applicant = applicantRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Applicant not found"));
        return ApplicantResponseDto.from(applicant);
    }

    public List<ApplicantResponseDto> getAll() {
        return applicantRepository.findAll().stream()
                .map(ApplicantResponseDto::from)
                .toList();
    }

    public Applicant getEntity(Long id) {
        return applicantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Applicant not found"));
    }
}
