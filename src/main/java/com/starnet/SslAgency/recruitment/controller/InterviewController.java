package com.starnet.SslAgency.recruitment.controller;

import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.recruitment.dto.InterviewDto;
import com.starnet.SslAgency.recruitment.dto.InterviewResponseDto;
import com.starnet.SslAgency.recruitment.model.Interview;
import com.starnet.SslAgency.recruitment.service.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recruitment/interviews")
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public InterviewResponseDto schedule(@RequestBody InterviewDto dto, @AuthenticationPrincipal Staff actor) {
        return interviewService.schedule(dto, actor);
    }

    @GetMapping("/by-application/{applicationId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<InterviewResponseDto> byApplication(@PathVariable Long applicationId) {
        return interviewService.listByApplication(applicationId);
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public InterviewResponseDto complete(@PathVariable Long id,
                                         @RequestBody Map<String, Object> body,
                                         @AuthenticationPrincipal Staff actor) {
        Interview.Outcome outcome = body.get("outcome") != null
                ? Interview.Outcome.valueOf(body.get("outcome").toString()) : null;
        Integer rating = body.get("rating") != null ? Integer.valueOf(body.get("rating").toString()) : null;
        return interviewService.complete(id, outcome, rating,
                body.get("notes") != null ? body.get("notes").toString() : null, actor);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public InterviewResponseDto cancel(@PathVariable Long id,
                                       @RequestBody(required = false) Map<String, String> body,
                                       @AuthenticationPrincipal Staff actor) {
        return interviewService.cancel(id,
                body != null ? body.get("reason") : null, actor);
    }

    @PatchMapping("/{id}/miss")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public InterviewResponseDto miss(@PathVariable Long id, @AuthenticationPrincipal Staff actor) {
        return interviewService.miss(id, actor);
    }

    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public InterviewResponseDto reschedule(@PathVariable Long id, @RequestBody InterviewDto dto,
                                           @AuthenticationPrincipal Staff actor) {
        return interviewService.reschedule(id, dto, actor);
    }
}