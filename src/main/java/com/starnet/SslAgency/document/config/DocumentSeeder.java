package com.starnet.SslAgency.document.config;

import com.starnet.SslAgency.applicant.model.Applicant;
import com.starnet.SslAgency.document.model.DocumentRequirement;
import com.starnet.SslAgency.document.model.DocumentType;
import com.starnet.SslAgency.document.repository.DocumentRequirementRepository;
import com.starnet.SslAgency.document.repository.DocumentTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DocumentSeeder implements CommandLineRunner {

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private DocumentRequirementRepository requirementRepository;

    @Override
    public void run(String... args) {
        if (documentTypeRepository.count() > 0) {
            return;
        }

        Map<String, DocumentType> types = new LinkedHashMap<>();
        types.put("PASSPORT", documentTypeRepository.save(
                DocumentType.builder().code("PASSPORT").name("Passport")
                        .description("Valid passport bio page").category(DocumentType.Category.TRAVEL).build()));
        types.put("NATIONAL_ID", documentTypeRepository.save(
                DocumentType.builder().code("NATIONAL_ID").name("National ID")
                        .description("Government issued national identity card").category(DocumentType.Category.IDENTITY).build()));
        types.put("KRA_PIN", documentTypeRepository.save(
                DocumentType.builder().code("KRA_PIN").name("KRA PIN Certificate")
                        .description("Tax registration certificate").category(DocumentType.Category.IDENTITY).build()));
        types.put("CERTIFICATE", documentTypeRepository.save(
                DocumentType.builder().code("CERTIFICATE").name("Academic Certificate")
                        .description("Highest academic certificate").category(DocumentType.Category.CERTIFICATION).build()));
        types.put("RESUME", documentTypeRepository.save(
                DocumentType.builder().code("RESUME").name("Resume / CV")
                        .description("Professional resume").requiresVerification(false).category(DocumentType.Category.EMPLOYMENT).build()));
        types.put("MEDICAL_CERT", documentTypeRepository.save(
                DocumentType.builder().code("MEDICAL_CERT").name("Medical Certificate")
                        .description("Current medical examination certificate").category(DocumentType.Category.MEDICAL).build()));
        types.put("REFERENCE_LETTER", documentTypeRepository.save(
                DocumentType.builder().code("REFERENCE_LETTER").name("Reference Letter")
                        .description("Employer or character reference").requiresVerification(false)
                        .category(DocumentType.Category.EMPLOYMENT).build()));
        types.put("PASS_PHOTO", documentTypeRepository.save(
                DocumentType.builder().code("PASS_PHOTO").name("Passport Photo")
                        .description("Recent passport style photograph").requiresVerification(false)
                        .category(DocumentType.Category.IDENTITY).build()));

        seedRequirement(types.get("PASSPORT"), Applicant.ApplicantType.INTERNATIONAL, true);
        seedRequirement(types.get("MEDICAL_CERT"), Applicant.ApplicantType.INTERNATIONAL, true);
        seedRequirement(types.get("RESUME"), Applicant.ApplicantType.INTERNATIONAL, true);
        seedRequirement(types.get("PASS_PHOTO"), Applicant.ApplicantType.INTERNATIONAL, true);

        seedRequirement(types.get("NATIONAL_ID"), Applicant.ApplicantType.LOCAL, true);
        seedRequirement(types.get("KRA_PIN"), Applicant.ApplicantType.LOCAL, true);
        seedRequirement(types.get("CERTIFICATE"), Applicant.ApplicantType.LOCAL, true);
        seedRequirement(types.get("RESUME"), Applicant.ApplicantType.LOCAL, true);
    }

    private void seedRequirement(DocumentType documentType, Applicant.ApplicantType applicantType, boolean required) {
        DocumentRequirement requirement = DocumentRequirement.builder()
                .documentType(documentType)
                .applicantType(applicantType)
                .required(required)
                .build();
        requirementRepository.save(requirement);
    }
}