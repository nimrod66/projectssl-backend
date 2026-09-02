package com.starnet.SslAgency.recruitment.dto;

import com.starnet.SslAgency.recruitment.model.Application;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationResponseDto {

    private Long id;
    private Long applicantId;
    private String applicantName;
    private String applicantNumber;
    private Long opportunityId;
    private String opportunityTitle;
    private Long assignedRecruiterId;
    private String assignedRecruiterName;
    private Application.Status status;
    private Application.RejectionReason rejectionReason;
    private String rejectionDetails;
    private LocalDateTime appliedAt;
    private LocalDateTime lastActivityAt;
    private LocalDateTime createdAt;
    private int interviewCount;
    private int offerCount;

    public static ApplicationResponseDto from(Application a) {
        return ApplicationResponseDto.builder()
                .id(a.getId())
                .applicantId(a.getApplicant().getId())
                .applicantName(a.getApplicant().getFirstName() + " " + a.getApplicant().getLastName())
                .applicantNumber(a.getApplicant().getApplicantNumber())
                .opportunityId(a.getOpportunity().getId())
                .opportunityTitle(a.getOpportunity().getTitle())
                .assignedRecruiterId(a.getAssignedRecruiter() != null ? a.getAssignedRecruiter().getId() : null)
                .assignedRecruiterName(a.getAssignedRecruiter() != null
                        ? a.getAssignedRecruiter().getFirstName() + " " + a.getAssignedRecruiter().getLastName()
                        : null)
                .status(a.getStatus())
                .rejectionReason(a.getRejectionReason())
                .rejectionDetails(a.getRejectionDetails())
                .appliedAt(a.getAppliedAt())
                .lastActivityAt(a.getLastActivityAt())
                .createdAt(a.getCreatedAt())
                .interviewCount(a.getInterviews() != null ? a.getInterviews().size() : 0)
                .offerCount(a.getOffers() != null ? a.getOffers().size() : 0)
                .build();
    }
}
