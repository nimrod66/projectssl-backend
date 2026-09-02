package com.starnet.SslAgency.communication.repository;

import com.starnet.SslAgency.communication.model.CommunicationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommunicationLogRepository extends JpaRepository<CommunicationLog, Long> {
    List<CommunicationLog> findByEntityIdAndEntityTypeOrderByCreatedAtDesc(Long entityId, String entityType);
    long countByEntityIdAndEntityType(Long entityId, String entityType);
}
