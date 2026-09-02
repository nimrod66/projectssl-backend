package com.starnet.SslAgency.placement.service;

import com.starnet.SslAgency.application.model.Application;
import com.starnet.SslAgency.application.repository.ApplicationRepository;
import com.starnet.SslAgency.common.CandidateNameResolver;
import com.starnet.SslAgency.interapplication.model.InterApplication;
import com.starnet.SslAgency.interapplication.repository.InterApplicationRepository;
import com.starnet.SslAgency.media.service.FileStorageService;
import com.starnet.SslAgency.notification.service.NotificationService;
import com.starnet.SslAgency.placement.dto.*;
import com.starnet.SslAgency.task.service.TaskService;
import com.starnet.SslAgency.placement.model.Placement;
import com.starnet.SslAgency.placement.model.PlacementDocument;
import com.starnet.SslAgency.placement.model.PlacementStatusHistory;
import com.starnet.SslAgency.placement.repository.PlacementDocumentRepository;
import com.starnet.SslAgency.placement.repository.PlacementRepository;
import com.starnet.SslAgency.placement.repository.PlacementStatusHistoryRepository;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.processor.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlacementService {

    @Autowired
    private PlacementRepository placementRepository;

    @Autowired
    private PlacementStatusHistoryRepository historyRepository;

    @Autowired
    private PlacementDocumentRepository documentRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private InterApplicationRepository interApplicationRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private CandidateNameResolver candidateNameResolver;

    public Placement getPlacement(Long id) {
        return placementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Placement not found"));
    }

    public List<Placement> getPlacementsByContract(Long contractId) {
        return placementRepository.findByContractId(contractId);
    }

    public List<PlacementStatusHistory> getPlacementHistory(Long placementId) {
        return historyRepository.findByPlacementIdOrderByChangedAtDesc(placementId);
    }

    @Transactional
    public Placement advanceStage(Long placementId, String stageName, String note, Long staffId) {
        Placement placement = getPlacement(placementId);
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff not found"));

        Placement.Stage newStage;
        try {
            newStage = Placement.Stage.valueOf(stageName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid stage: " + stageName);
        }

        PlacementStatusHistory history = PlacementStatusHistory.builder()
                .placement(placement)
                .stage(newStage)
                .note(note)
                .changedBy(staff)
                .changedAt(LocalDateTime.now())
                .build();
        historyRepository.save(history);

        placement.setStage(newStage);
        placement = placementRepository.save(placement);

        if (newStage == Placement.Stage.DEPLOYED) {
            markCandidateHired(placement, staff);
        }

        triggerStageNotification(placement, newStage);
        taskService.generateTasksForStage(placement, newStage, staff);
        return placement;
    }

    @Transactional
    public Placement addNote(Long placementId, String note, Long staffId) {
        Placement placement = getPlacement(placementId);
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff not found"));

        PlacementStatusHistory history = PlacementStatusHistory.builder()
                .placement(placement)
                .stage(placement.getStage())
                .note(note)
                .changedBy(staff)
                .changedAt(LocalDateTime.now())
                .build();
        historyRepository.save(history);

        return placement;
    }

    @Transactional
    public PlacementDocument uploadDocument(Long placementId, MultipartFile file, String docKind, Long staffId) throws IOException {
        Placement placement = getPlacement(placementId);
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff not found"));

        FileStorageService.StoredFile stored = fileStorageService.store(file);

        PlacementDocument doc = PlacementDocument.builder()
                .placement(placement)
                .fileName(stored.originalName())
                .fileUrl(stored.fileUrl())
                .fileType(stored.contentType())
                .docKind(PlacementDocument.DocKind.valueOf(docKind.toUpperCase()))
                .uploadedBy(staff)
                .uploadedAt(LocalDateTime.now())
                .build();
        return documentRepository.save(doc);
    }

    public List<PlacementDocument> getDocuments(Long placementId) {
        return documentRepository.findByPlacementId(placementId);
    }

    @Transactional
    public void deleteDocument(Long documentId) {
        PlacementDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        if (!doc.getFileUrl().startsWith("http")) {
            fileStorageService.delete(doc.getFileUrl());
        }
        documentRepository.delete(doc);
    }

    private void markCandidateHired(Placement placement, Staff staff) {
        if (placement.getApplicationId() != null) {
            Application app = applicationRepository.findById(placement.getApplicationId())
                    .orElse(null);
            if (app != null && app.getStatus() != Application.Status.HIRED) {
                app.setStatus(Application.Status.HIRED);
                app.setHiredBy(staff);
                app.setHiredAt(LocalDateTime.now());
                applicationRepository.save(app);
            }
        } else if (placement.getInterApplicationId() != null) {
            InterApplication inter = interApplicationRepository.findById(placement.getInterApplicationId())
                    .orElse(null);
            if (inter != null && inter.getStatus() != InterApplication.Status.HIRED) {
                inter.setStatus(InterApplication.Status.HIRED);
                inter.setHiredBy(staff);
                inter.setHiredAt(LocalDateTime.now());
                interApplicationRepository.save(inter);
            }
        }
    }

    private void triggerStageNotification(Placement p, Placement.Stage stage) {
        String name = getCandidateName(p);
        String employer = p.getContract() != null && p.getContract().getEmployer() != null ? p.getContract().getEmployer().getCompanyName() : "";
        switch (stage) {
            case VISA_APPROVED -> notificationService.create(name + " - Visa approved for " + employer, "PLACEMENT_ACTION", p.getId(), "placement");
            case FLIGHT_BOOKED -> notificationService.create(name + " - Flight booked for " + employer, "PLACEMENT_ACTION", p.getId(), "placement");
            case DEPLOYED -> notificationService.create(name + " deployed to " + employer, "DEPLOYMENT", p.getId(), "placement");
            case COMPLETED -> notificationService.create(name + " - Contract with " + employer + " completed", "STATUS_CHANGE", p.getId(), "placement");
        }
    }

    private String getCandidateName(Placement p) {
        return candidateNameResolver.resolve(p);
    }

    public PlacementResponseDto toResponseDto(Placement p) {
        return PlacementResponseDto.builder()
                .id(p.getId())
                .contractId(p.getContract() != null ? p.getContract().getId() : null)
                .jobCategory(p.getContract() != null ? p.getContract().getJobCategory() : null)
                .country(p.getContract() != null ? p.getContract().getCountry() : null)
                .applicationId(p.getApplicationId())
                .interApplicationId(p.getInterApplicationId())
                .candidateType(p.getCandidateType())
                .candidateName(getCandidateName(p))
                .stage(p.getStage().name())
                .contractStartDate(p.getContractStartDate())
                .contractEndDate(p.getContractEndDate())
                .salary(p.getSalary())
                .currency(p.getCurrency())
                .notes(p.getNotes())
                .assignedBy(p.getAssignedBy() != null ? p.getAssignedBy().getId() : null)
                .assignedAt(p.getAssignedAt() != null ? p.getAssignedAt().toString() : null)
                .createdAt(p.getCreatedAt() != null ? p.getCreatedAt().toString() : null)
                .updatedAt(p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : null)
                .employerName(p.getContract() != null && p.getContract().getEmployer() != null
                        ? p.getContract().getEmployer().getCompanyName() : null)
                .build();
    }

    public PlacementDocumentResponseDto toDocDto(PlacementDocument d) {
        return PlacementDocumentResponseDto.builder()
                .id(d.getId())
                .placementId(d.getPlacement() != null ? d.getPlacement().getId() : null)
                .fileName(d.getFileName())
                .fileUrl(d.getFileUrl())
                .fileType(d.getFileType())
                .docKind(d.getDocKind().name())
                .uploadedBy(d.getUploadedBy() != null ? d.getUploadedBy().getId() : null)
                .uploadedByName(d.getUploadedBy() != null ? d.getUploadedBy().getFirstName() + " " + d.getUploadedBy().getLastName() : null)
                .uploadedAt(d.getUploadedAt() != null ? d.getUploadedAt().toString() : null)
                .build();
    }

    public PlacementStatusHistoryDto toHistoryDto(PlacementStatusHistory h) {
        return PlacementStatusHistoryDto.builder()
                .id(h.getId())
                .stage(h.getStage().name())
                .note(h.getNote())
                .changedBy(h.getChangedBy() != null ? h.getChangedBy().getId() : null)
                .changedByName(h.getChangedBy() != null ? h.getChangedBy().getFirstName() + " " + h.getChangedBy().getLastName() : null)
                .changedAt(h.getChangedAt())
                .build();
    }
}
