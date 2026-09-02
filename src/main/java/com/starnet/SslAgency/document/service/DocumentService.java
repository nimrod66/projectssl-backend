package com.starnet.SslAgency.document.service;

import com.starnet.SslAgency.applicant.model.Applicant;
import com.starnet.SslAgency.applicant.model.ApplicantTimeline;
import com.starnet.SslAgency.applicant.service.ApplicantService;
import com.starnet.SslAgency.applicant.service.ApplicantTimelineService;
import com.starnet.SslAgency.document.dto.*;
import com.starnet.SslAgency.document.model.*;
import com.starnet.SslAgency.document.repository.*;
import com.starnet.SslAgency.media.service.FileStorageService;
import com.starnet.SslAgency.opportunity.repository.OpportunityRepository;
import com.starnet.SslAgency.placement.core.model.Placement;
import com.starnet.SslAgency.placement.core.service.PlacementService;
import com.starnet.SslAgency.processor.model.Staff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private DocumentRequirementRepository requirementRepository;

    @Autowired
    private FileAssetRepository fileAssetRepository;

    @Autowired
    private ApplicantDocumentRepository applicantDocumentRepository;

    @Autowired
    private ApplicantMediaRepository applicantMediaRepository;

    @Autowired
    private CorePlacementDocumentRepository placementDocumentRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ApplicantService applicantService;

    @Autowired
    private PlacementService placementService;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private ApplicantTimelineService timelineService;

    public List<DocumentTypeDto> listDocumentTypes() {
        return documentTypeRepository.findAll().stream()
                .map(DocumentTypeDto::from)
                .toList();
    }

    public List<DocumentRequirementDto> listRequirements(Applicant.ApplicantType applicantType, Long opportunityId) {
        if (opportunityId != null) {
            return requirementRepository.findByOpportunityIdOrderByIdAsc(opportunityId).stream()
                    .map(DocumentRequirementDto::from)
                    .toList();
        }
        if (applicantType != null) {
            return requirementRepository.findByApplicantTypeOrderByIdAsc(applicantType).stream()
                    .map(DocumentRequirementDto::from)
                    .toList();
        }
        return requirementRepository.findAll().stream()
                .map(DocumentRequirementDto::from)
                .toList();
    }

    @Transactional
    public DocumentRequirementDto createRequirement(DocumentRequirementRequestDto dto) {
        DocumentType documentType;
        if (dto.getDocumentTypeId() != null) {
            documentType = documentTypeRepository.findById(dto.getDocumentTypeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document type not found"));
        } else {
            documentType = documentTypeRepository.findByCode(dto.getDocumentTypeCode())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document type not found"));
        }

        DocumentRequirement requirement = DocumentRequirement.builder()
                .documentType(documentType)
                .applicantType(dto.getApplicantType())
                .opportunity(dto.getOpportunityId() != null
                        ? opportunityRepository.findById(dto.getOpportunityId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opportunity not found"))
                        : null)
                .required(dto.isRequired())
                .build();

        return DocumentRequirementDto.from(requirementRepository.save(requirement));
    }

    @Transactional
    public ApplicantDocumentDto uploadApplicantDocument(Long applicantId, Long documentTypeId,
                                                        MultipartFile file, Staff actor) {
        Applicant applicant = applicantService.getEntity(applicantId);
        DocumentType documentType = documentTypeRepository.findById(documentTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document type not found"));

        FileAsset asset = storeFile(file, actor);

        int nextVersion = 1;
        Optional<ApplicantDocument> current = applicantDocumentRepository
                .findByApplicantIdAndDocumentTypeIdAndCurrentTrue(applicantId, documentTypeId);
        if (current.isPresent()) {
            current.get().setCurrent(false);
            applicantDocumentRepository.save(current.get());
            nextVersion = current.get().getVersion() + 1;
        }

        ApplicantDocument document = ApplicantDocument.builder()
                .applicant(applicant)
                .documentType(documentType)
                .fileAsset(asset)
                .status(ApplicantDocument.Status.UPLOADED)
                .version(nextVersion)
                .current(true)
                .build();
        document = applicantDocumentRepository.save(document);

        timelineService.log(applicant, ApplicantTimeline.EventType.DOCUMENT_UPLOADED,
                "Document uploaded: " + documentType.getName(), actor,
                "documentType=" + documentType.getCode() + ";version=" + nextVersion);

        return ApplicantDocumentDto.from(document);
    }

    @Transactional
    public ApplicantDocumentDto verifyApplicantDocument(Long documentId, Staff actor) {
        ApplicantDocument document = getApplicantDocument(documentId);
        if (document.getFileAsset() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document has no file to verify");
        }
        if (document.getStatus() == ApplicantDocument.Status.VERIFIED) {
            return ApplicantDocumentDto.from(document);
        }
        document.setStatus(ApplicantDocument.Status.VERIFIED);
        document.setVerifiedBy(actor);
        document.setVerifiedAt(LocalDateTime.now());
        document.setRejectionReason(null);
        document = applicantDocumentRepository.save(document);

        timelineService.log(document.getApplicant(), ApplicantTimeline.EventType.DOCUMENT_VERIFIED,
                "Document verified: " + document.getDocumentType().getName(), actor,
                "documentType=" + document.getDocumentType().getCode());

        return ApplicantDocumentDto.from(document);
    }

    @Transactional
    public ApplicantDocumentDto rejectApplicantDocument(Long documentId, String reason, Staff actor) {
        ApplicantDocument document = getApplicantDocument(documentId);
        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rejection reason is mandatory");
        }
        document.setStatus(ApplicantDocument.Status.REJECTED);
        document.setRejectionReason(reason);
        document = applicantDocumentRepository.save(document);

        timelineService.log(document.getApplicant(), ApplicantTimeline.EventType.DOCUMENT_REJECTED,
                "Document rejected: " + document.getDocumentType().getName() + " - " + reason, actor,
                "documentType=" + document.getDocumentType().getCode());

        return ApplicantDocumentDto.from(document);
    }

    @Transactional
    public ApplicantDocumentDto requestResubmission(Long documentId, String reason, Staff actor) {
        ApplicantDocument document = getApplicantDocument(documentId);
        document.setStatus(ApplicantDocument.Status.RESUBMISSION_REQUIRED);
        document.setRejectionReason(reason);
        document = applicantDocumentRepository.save(document);
        return ApplicantDocumentDto.from(document);
    }

    public List<ApplicantDocumentDto> listApplicantDocuments(Long applicantId) {
        return applicantDocumentRepository.findByApplicantIdOrderByDocumentTypeAscVersionDesc(applicantId).stream()
                .map(ApplicantDocumentDto::from)
                .toList();
    }

    public List<ApplicantDocumentDto> listCurrentApplicantDocuments(Long applicantId) {
        return applicantDocumentRepository.findByApplicantIdAndCurrentTrueOrderByDocumentTypeAsc(applicantId).stream()
                .map(ApplicantDocumentDto::from)
                .toList();
    }

    @Transactional
    public ApplicantMediaDto uploadApplicantMedia(Long applicantId, ApplicantMedia.MediaType mediaType,
                                                  MultipartFile file, String description, Staff actor) {
        Applicant applicant = applicantService.getEntity(applicantId);
        FileAsset asset = storeFile(file, actor);

        ApplicantMedia media = ApplicantMedia.builder()
                .applicant(applicant)
                .fileAsset(asset)
                .mediaType(mediaType != null ? mediaType : ApplicantMedia.MediaType.OTHER)
                .description(description)
                .build();
        return ApplicantMediaDto.from(applicantMediaRepository.save(media));
    }

    public List<ApplicantMediaDto> listApplicantMedia(Long applicantId) {
        return applicantMediaRepository.findByApplicantIdOrderByUploadedAtDesc(applicantId).stream()
                .map(ApplicantMediaDto::from)
                .toList();
    }

    @Transactional
    public PlacementDocumentDto uploadPlacementDocument(Long placementId, Long documentTypeId,
                                                        MultipartFile file, Staff actor) {
        Placement placement = placementService.getEntity(placementId);
        DocumentType documentType = documentTypeRepository.findById(documentTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document type not found"));
        FileAsset asset = storeFile(file, actor);

        PlacementDocument document = PlacementDocument.builder()
                .placement(placement)
                .documentType(documentType)
                .fileAsset(asset)
                .status(PlacementDocument.Status.UPLOADED)
                .build();
        return PlacementDocumentDto.from(placementDocumentRepository.save(document));
    }

    @Transactional
    public PlacementDocumentDto verifyPlacementDocument(Long documentId, Staff actor) {
        PlacementDocument document = placementDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Placement document not found"));
        document.setStatus(PlacementDocument.Status.VERIFIED);
        document.setVerifiedBy(actor);
        document.setVerifiedAt(LocalDateTime.now());
        return PlacementDocumentDto.from(placementDocumentRepository.save(document));
    }

    public List<PlacementDocumentDto> listPlacementDocuments(Long placementId) {
        return placementDocumentRepository.findByPlacementIdOrderByUploadedAtDesc(placementId).stream()
                .map(PlacementDocumentDto::from)
                .toList();
    }

    private ApplicantDocument getApplicantDocument(Long documentId) {
        return applicantDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Applicant document not found"));
    }

    private FileAsset storeFile(MultipartFile file, Staff actor) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file provided");
        }
        FileStorageService.StoredFile stored;
        try {
            stored = fileStorageService.store(file);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File storage failed: " + e.getMessage());
        }
        FileAsset asset = FileAsset.builder()
                .originalName(stored.originalName())
                .storedName(stored.storedName())
                .contentType(stored.contentType())
                .fileUrl(stored.fileUrl())
                .sizeBytes(stored.size())
                .uploadedBy(actor)
                .build();
        return fileAssetRepository.save(asset);
    }
}