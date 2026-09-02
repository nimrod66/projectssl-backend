package com.starnet.SslAgency.recruitment.dto;

import com.starnet.SslAgency.recruitment.model.Interview;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewResponseDto {

    private Long id;
    private Long applicationId;
    private Interview.Type type;
    private LocalDateTime scheduledAt;
    private Long interviewerId;
    private String interviewerName;
    private String location;
    private String meetingLink;
    private Interview.Status status;
    private Interview.Outcome outcome;
    private Integer rating;
    private String notes;

    public static InterviewResponseDto from(Interview i) {
        return InterviewResponseDto.builder()
                .id(i.getId())
                .applicationId(i.getApplication().getId())
                .type(i.getType())
                .scheduledAt(i.getScheduledAt())
                .interviewerId(i.getInterviewer() != null ? i.getInterviewer().getId() : null)
                .interviewerName(i.getInterviewer() != null
                        ? i.getInterviewer().getFirstName() + " " + i.getInterviewer().getLastName()
                        : null)
                .location(i.getLocation())
                .meetingLink(i.getMeetingLink())
                .status(i.getStatus())
                .outcome(i.getOutcome())
                .rating(i.getRating())
                .notes(i.getNotes())
                .build();
    }
}
