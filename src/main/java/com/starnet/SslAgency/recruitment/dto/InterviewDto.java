package com.starnet.SslAgency.recruitment.dto;

import com.starnet.SslAgency.recruitment.model.Interview;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewDto {

    private Long applicationId;

    private Interview.Type type;

    private LocalDateTime scheduledAt;

    private Long interviewerId;

    private String location;

    private String meetingLink;

    private Interview.Status status;

    private Interview.Outcome outcome;

    private Integer rating;

    private String notes;
}
