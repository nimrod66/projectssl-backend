package com.starnet.SslAgency.application.controller;

import com.starnet.SslAgency.application.model.Application;
import com.starnet.SslAgency.application.repository.ApplicationRepository;
import com.starnet.SslAgency.media.model.MediaFile;
import com.starnet.SslAgency.placement.model.Placement;
import com.starnet.SslAgency.placement.repository.PlacementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/candidates")
public class CandidateWorkspaceController {

    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private PlacementRepository placementRepository;

    @GetMapping("/local/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public Map<String, Object> getLocalCandidate(@PathVariable Long id) {
        Application a = applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Applicant not found"));
        return buildResponse(a);
    }

    private Map<String, Object> buildResponse(Application a) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id", a.getId());
        resp.put("fullName", Stream.of(a.getFirstName(), a.getMiddleName(), a.getLastName()).filter(s -> s != null && !s.isBlank()).collect(Collectors.joining(" ")));
        resp.put("firstName", a.getFirstName());
        resp.put("lastName", a.getLastName());
        resp.put("age", a.getAge());
        resp.put("nationality", a.getNationality());
        resp.put("phoneNumber", a.getPhoneNumber());
        resp.put("email", a.getEmail());
        resp.put("currentLocation", a.getCurrentLocation());
        resp.put("experience", a.getExperience());
        resp.put("currentProfession", a.getCurrentProfession());
        resp.put("currentSalary", a.getCurrentSalary());
        resp.put("languages", a.getLanguages() != null ? a.getLanguages().stream().map(Enum::name).toList() : List.of());
        resp.put("employmentStatus", a.getEmploymentStatus() != null ? a.getEmploymentStatus().name() : null);
        resp.put("jobInterest", a.getJobInterest() != null ? a.getJobInterest().name() : null);
        resp.put("status", a.getStatus().name());

        resp.put("details", Map.of(
                "hasCat", a.getHasCat() != null ? a.getHasCat() : false,
                "hasDog", a.getHasDog() != null ? a.getHasDog() : false,
                "extraPay", a.getExtraPay() != null ? a.getExtraPay() : false,
                "liveOut", a.getLiveOut() != null ? a.getLiveOut() : false,
                "privateRoom", a.getPrivateRoom() != null ? a.getPrivateRoom() : false,
                "elderlyCare", a.getElderlyCare() != null ? a.getElderlyCare() : false,
                "specialNeeds", a.getSpecialNeeds() != null ? a.getSpecialNeeds() : false,
                "olderThan1", a.getOlderThan1() != null ? a.getOlderThan1() : false,
                "youngerThan1", a.getYoungerThan1() != null ? a.getYoungerThan1() : false
        ));

        List<Map<String, Object>> docs = new ArrayList<>();
        if (a.getMediaFiles() != null) {
            for (MediaFile m : a.getMediaFiles()) {
                Map<String, Object> d = new LinkedHashMap<>();
                d.put("id", m.getId());
                d.put("fileName", m.getFileName());
                d.put("fileUrl", m.getFileUrl());
                d.put("fileType", m.getFileType());
                d.put("kind", m.getKind().name());
                docs.add(d);
            }
        }
        resp.put("documents", docs);

        List<Map<String, Object>> timeline = new ArrayList<>();
        addTimelineEvent(timeline, a.getCreatedAt(), "Application submitted", "REGISTERED", null);
        if (a.getVettedAt() != null) addTimelineEvent(timeline, a.getVettedAt(), "Vetted by " + (a.getVettedBy() != null ? a.getVettedBy().getFirstName() + " " + a.getVettedBy().getLastName() : "staff"), "VETTED", a.getVettedBy() != null ? a.getVettedBy().getFirstName() + " " + a.getVettedBy().getLastName() : null);
        if (a.getApprovedAt() != null) addTimelineEvent(timeline, a.getApprovedAt(), "Approved by " + (a.getApprovedBy() != null ? a.getApprovedBy().getFirstName() + " " + a.getApprovedBy().getLastName() : "staff"), "APPROVED", a.getApprovedBy() != null ? a.getApprovedBy().getFirstName() + " " + a.getApprovedBy().getLastName() : null);
        if (a.getHiredAt() != null) addTimelineEvent(timeline, a.getHiredAt(), "Hired / Deployed", "HIRED", a.getHiredBy() != null ? a.getHiredBy().getFirstName() + " " + a.getHiredBy().getLastName() : null);
        if (a.getStatus() == Application.Status.REJECTED && a.getVettedAt() != null) {
            addTimelineEvent(timeline, a.getVettedAt(), "Application rejected", "REJECTED", null);
        }
        timeline.sort((x, y) -> ((String) x.get("timestamp")).compareTo((String) y.get("timestamp")));
        resp.put("timeline", timeline);

        List<Placement> placements = placementRepository.findByApplicationId(a.getId());
        resp.put("placements", placements.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<String, Object>();
            m.put("id", p.getId());
            m.put("contractId", p.getContract() != null ? p.getContract().getId() : null);
            m.put("employerName", p.getContract() != null && p.getContract().getEmployer() != null ? p.getContract().getEmployer().getCompanyName() : "N/A");
            m.put("stage", p.getStage().name());
            m.put("startDate", p.getContractStartDate() != null ? p.getContractStartDate().toString() : null);
            m.put("endDate", p.getContractEndDate() != null ? p.getContractEndDate().toString() : null);
            m.put("salary", p.getSalary());
            m.put("currency", p.getCurrency());
            return m;
        }).toList());

        return resp;
    }

    private void addTimelineEvent(List<Map<String, Object>> list, Object timestamp, String description, String type, String actor) {
        if (timestamp == null) return;
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("timestamp", timestamp.toString());
        event.put("description", description);
        event.put("type", type);
        event.put("actor", actor);
        list.add(event);
    }
}
