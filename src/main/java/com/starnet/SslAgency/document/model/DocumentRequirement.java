package com.starnet.SslAgency.document.model;

import com.starnet.SslAgency.applicant.model.Applicant;
import com.starnet.SslAgency.opportunity.model.Opportunity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "document_requirements", indexes = {
        @Index(name = "idx_docreq_type", columnList = "document_type_id"),
        @Index(name = "idx_docreq_opportunity", columnList = "opportunity_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_type_id", nullable = false)
    @ToString.Exclude
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicant_type", nullable = false, length = 20)
    private Applicant.ApplicantType applicantType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id")
    @ToString.Exclude
    private Opportunity opportunity;

    @Column(nullable = false)
    @Builder.Default
    private boolean required = true;
}