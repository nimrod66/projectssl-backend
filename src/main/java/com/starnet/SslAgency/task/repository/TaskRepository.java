package com.starnet.SslAgency.task.repository;

import com.starnet.SslAgency.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByAssignedToIdOrderByDueDateAsc(Long staffId);

    List<Task> findByStatusOrderByDueDateAsc(Task.Status status);

    List<Task> findByRelatedApplicantIdOrderByCreatedAtDesc(Long applicantId);

    List<Task> findAllByOrderByCreatedAtDesc();
}