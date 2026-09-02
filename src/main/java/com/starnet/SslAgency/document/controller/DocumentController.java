package com.starnet.SslAgency.document.controller;

import com.starnet.SslAgency.applicant.model.Applicant;
import com.starnet.SslAgency.document.dto.*;
import com.starnet.SslAgency.document.model.ApplicantMedia;
import com.starnet.SslAgency.document.service.DocumentService;
import com.starnet.SslAgency.processor.model.Staff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping("/types")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<DocumentTypeDto> types() {
        return documentService.listDocumentTypes();
    }

    @GetMapping("/requirements")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public List<DocumentRequirementDto> requirements(@RequestParam(required = false) Applicant.ApplicantType applicantType,
                                                     @RequestParam(required = false) Long opportunityId) {
        return documentService.listRequirements(applicantType, opportunityId);
    }

    @PostMapping("/requirements")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public DocumentRequirementDto createRequirement(@RequestBody DocumentRequirementRequestDto dto) {
        return documentService.createRequirement(dto);
    }

    @GetMapping("/applicant/{applicantId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<ApplicantDocumentDto> applicantDocuments(@PathVariable Long applicantId) {
        return documentService.listApplicantDocuments(applicantId);
    }

    @GetMapping("/applicant/{applicantId}/current")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<ApplicantDocumentDto> currentApplicantDocuments(@PathVariable Long applicantId) {
        return documentService.listCurrentApplicantDocuments(applicantId);
    }

    @PostMapping(value = "/applicant/{applicantId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public ApplicantDocumentDto uploadApplicantDocument(@PathVariable Long applicantId,
                                                        @RequestParam("documentTypeId") Long documentTypeId,
                                                        @RequestParam("file") MultipartFile file,
                                                        @AuthenticationPrincipal Staff actor) {
        return documentService.uploadApplicantDocument(applicantId, documentTypeId, file, actor);
    }

    @PatchMapping("/applicant/{documentId}/verify")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public ApplicantDocumentDto verifyApplicantDocument(@PathVariable Long documentId,
                                                        @AuthenticationPrincipal Staff actor) {
        return documentService.verifyApplicantDocument(documentId, actor);
    }

    @PatchMapping("/applicant/{documentId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public ApplicantDocumentDto rejectApplicantDocument(@PathVariable Long documentId,
                                                        @RequestBody(required = false) java.util.Map<String, String> body,
                                                        @AuthenticationPrincipal Staff actor) {
        return documentService.rejectApplicantDocument(documentId,
                body != null ? body.get("reason") : null, actor);
    }

    @PatchMapping("/applicant/{documentId}/resubmission-required")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public ApplicantDocumentDto requestResubmission(@PathVariable Long documentId,
                                                    @RequestBody(required = false) java.util.Map<String, String> body,
                                                    @AuthenticationPrincipal Staff actor) {
        return documentService.requestResubmission(documentId,
                body != null ? body.get("reason") : null, actor);
    }

    @GetMapping("/applicant/{applicantId}/media")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<ApplicantMediaDto> applicantMedia(@PathVariable Long applicantId) {
        return documentService.listApplicantMedia(applicantId);
    }

    @PostMapping(value = "/applicant/{applicantId}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public ApplicantMediaDto uploadApplicantMedia(@PathVariable Long applicantId,
                                                  @RequestParam("mediaType") ApplicantMedia.MediaType mediaType,
                                                  @RequestParam("file") MultipartFile file,
                                                  @RequestParam(value = "description", required = false) String description,
                                                  @AuthenticationPrincipal Staff actor) {
        return documentService.uploadApplicantMedia(applicantId, mediaType, file, description, actor);
    }

    @GetMapping("/placement/{placementId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<PlacementDocumentDto> placementDocuments(@PathVariable Long placementId) {
        return documentService.listPlacementDocuments(placementId);
    }

    @PostMapping(value = "/placement/{placementId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public PlacementDocumentDto uploadPlacementDocument(@PathVariable Long placementId,
                                                        @RequestParam("documentTypeId") Long documentTypeId,
                                                        @RequestParam("file") MultipartFile file,
                                                        @AuthenticationPrincipal Staff actor) {
        return documentService.uploadPlacementDocument(placementId, documentTypeId, file, actor);
    }

    @PatchMapping("/placement/{documentId}/verify")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public PlacementDocumentDto verifyPlacementDocument(@PathVariable Long documentId,
                                                        @AuthenticationPrincipal Staff actor) {
        return documentService.verifyPlacementDocument(documentId, actor);
    }
}