package com.starnet.SslAgency.task.service;

import com.starnet.SslAgency.placement.model.Placement;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.processor.repository.StaffRepository;
import com.starnet.SslAgency.task.dto.TaskRequestDto;
import com.starnet.SslAgency.task.dto.TaskResponseDto;
import com.starnet.SslAgency.task.model.Task;
import com.starnet.SslAgency.task.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Transactional
    public TaskResponseDto create(TaskRequestDto dto, Staff createdBy) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is mandatory");
        }
        Task task = Task.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .priority(dto.getPriority() != null ? dto.getPriority() : Task.Priority.MEDIUM)
                .assignedTo(dto.getAssignedToId() != null
                        ? staffRepository.findById(dto.getAssignedToId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignee not found"))
                        : null)
                .relatedApplicantId(dto.getRelatedApplicantId())
                .relatedOpportunityId(dto.getRelatedOpportunityId())
                .dueDate(dto.getDueDate())
                .createdBy(createdBy)
                .build();
        return TaskResponseDto.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponseDto update(Long id, TaskRequestDto dto) {
        Task task = getEntity(id);
        if (task.getStatus() == Task.Status.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Completed tasks cannot be edited");
        }
        if (dto.getTitle() != null) {
            task.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            task.setDescription(dto.getDescription());
        }
        if (dto.getPriority() != null) {
            task.setPriority(dto.getPriority());
        }
        if (dto.getAssignedToId() != null) {
            task.setAssignedTo(staffRepository.findById(dto.getAssignedToId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignee not found")));
        }
        if (dto.getDueDate() != null) {
            task.setDueDate(dto.getDueDate());
        }
        taskRepository.save(task);
        return TaskResponseDto.from(task);
    }

    @Transactional
    public TaskResponseDto transition(Long id, Task.Status target) {
        Task task = getEntity(id);
        if (target == Task.Status.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
        }
        task.setStatus(target);
        taskRepository.save(task);
        return TaskResponseDto.from(task);
    }

    public TaskResponseDto get(Long id) {
        return TaskResponseDto.from(getEntity(id));
    }

    public List<TaskResponseDto> listAll() {
        return taskRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(TaskResponseDto::from)
                .toList();
    }

    public List<TaskResponseDto> listByAssignee(Long staffId) {
        return taskRepository.findByAssignedToIdOrderByDueDateAsc(staffId).stream()
                .map(TaskResponseDto::from)
                .toList();
    }

    public List<TaskResponseDto> listByStatus(Task.Status status) {
        return taskRepository.findByStatusOrderByDueDateAsc(status).stream()
                .map(TaskResponseDto::from)
                .toList();
    }

    public List<TaskResponseDto> listByApplicant(Long applicantId) {
        return taskRepository.findByRelatedApplicantIdOrderByCreatedAtDesc(applicantId).stream()
                .map(TaskResponseDto::from)
                .toList();
    }

    public Task getEntity(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    public List<Task> getTasksByStaff(Long staffId) {
        if (staffId == null) {
            return taskRepository.findAllByOrderByCreatedAtDesc();
        }
        return taskRepository.findByAssignedToIdOrderByDueDateAsc(staffId);
    }

    public void updateStatus(Long id, Task.Status status) {
        Task task = getEntity(id);
        task.setStatus(status);
        if (status == Task.Status.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
        }
        taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public void generateTasksForStage(Placement placement, Placement.Stage stage, Staff staff) {
        Task task = Task.builder()
                .title("Follow-up required for placement #" + placement.getId() + " at stage " + stage)
                .description("Placement " + placement.getId() + " moved to stage " + stage + "."
                        + (placement.getContractEndDate() != null
                        ? " Contract end: " + placement.getContractEndDate() : ""))
                .priority(Task.Priority.MEDIUM)
                .assignedTo(staff)
                .entityType("PLACEMENT")
                .entityId(placement.getId())
                .dueDate(placement.getContractEndDate() != null ? placement.getContractEndDate() : null)
                .createdBy(staff)
                .build();
        taskRepository.save(task);
    }
}