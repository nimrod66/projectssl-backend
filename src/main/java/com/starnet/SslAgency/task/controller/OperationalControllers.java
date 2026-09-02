package com.starnet.SslAgency.task.controller;

import com.starnet.SslAgency.placement.model.Placement;
import com.starnet.SslAgency.placement.repository.PlacementRepository;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.task.model.Task;
import com.starnet.SslAgency.task.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@RestController
public class OperationalControllers {

    @Autowired private TaskService taskService;
    @Autowired private PlacementRepository placementRepo;

    @GetMapping("/api/tasks")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<Map<String, Object>> getMyTasks(Authentication auth) {
        Staff staff = getStaff(auth);
        return taskService.getTasksByStaff(staff.getId()).stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId()); m.put("title", t.getTitle()); m.put("description", t.getDescription());
            m.put("status", t.getStatus().name()); m.put("priority", t.getPriority().name());
            m.put("dueDate", t.getDueDate() != null ? t.getDueDate().toString() : null);
            m.put("entityId", t.getEntityId()); m.put("entityType", t.getEntityType());
            return m;
        }).toList();
    }

    @PatchMapping("/api/tasks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Task.Status s = Task.Status.valueOf(body.get("status").toUpperCase());
        taskService.updateStatus(id, s);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/api/calendar")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
    public List<Map<String, Object>> getCalendar(@RequestParam int year, @RequestParam int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        List<Map<String, Object>> events = new ArrayList<>();

        taskService.getTasksByStaff(null).stream()
                .filter(t -> t.getDueDate() != null && !t.getDueDate().isBefore(start) && !t.getDueDate().isAfter(end))
                .forEach(t -> {
                    Map<String, Object> e = new LinkedHashMap<>();
                    e.put("date", t.getDueDate().toString()); e.put("title", t.getTitle());
                    e.put("type", "task"); e.put("status", t.getStatus().name()); e.put("id", t.getId());
                    events.add(e);
                });

        placementRepo.findAll().stream()
                .filter(p -> p.getContractEndDate() != null && !p.getContractEndDate().isBefore(start) && !p.getContractEndDate().isAfter(end))
                .forEach(p -> {
                    Map<String, Object> e = new LinkedHashMap<>();
                    e.put("date", p.getContractEndDate().toString()); e.put("title", "Contract ends: placement #" + p.getId());
                    e.put("type", "contract_end"); e.put("id", p.getId());
                    events.add(e);
                });

        return events;
    }

    private Staff getStaff(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Staff s)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        return s;
    }
}
