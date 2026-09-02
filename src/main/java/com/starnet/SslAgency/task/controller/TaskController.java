package com.starnet.SslAgency.task.controller;

import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.task.dto.TaskRequestDto;
import com.starnet.SslAgency.task.dto.TaskResponseDto;
import com.starnet.SslAgency.task.model.Task;
import com.starnet.SslAgency.task.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recruitment/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public TaskResponseDto create(@RequestBody TaskRequestDto dto, @AuthenticationPrincipal Staff actor) {
        return taskService.create(dto, actor);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public TaskResponseDto update(@PathVariable Long id, @RequestBody TaskRequestDto dto) {
        return taskService.update(id, dto);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER')")
    public TaskResponseDto transition(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return taskService.transition(id, Task.Status.valueOf(body.get("status")));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public TaskResponseDto get(@PathVariable Long id) {
        return taskService.get(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<TaskResponseDto> list(@RequestParam(required = false) String status,
                                      @RequestParam(required = false) Long assignedToId) {
        if (assignedToId != null) {
            return taskService.listByAssignee(assignedToId);
        }
        if (status != null && !status.isBlank()) {
            return taskService.listByStatus(Task.Status.valueOf(status));
        }
        return taskService.listAll();
    }

    @GetMapping("/by-applicant/{applicantId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<TaskResponseDto> byApplicant(@PathVariable Long applicantId) {
        return taskService.listByApplicant(applicantId);
    }
}