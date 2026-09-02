package com.starnet.SslAgency.audit.repository;

import com.starnet.SslAgency.audit.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityIdAndEntityTypeOrderByTimestampDesc(Long entityId, String entityType);
    long countByEntityIdAndEntityType(Long entityId, String entityType);
}
