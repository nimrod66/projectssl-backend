package com.starnet.SslAgency.audit.service;

import com.starnet.SslAgency.audit.model.AuditLog;
import com.starnet.SslAgency.audit.repository.AuditLogRepository;
import com.starnet.SslAgency.processor.model.Staff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    @Autowired private AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String entityType, Long entityId, String action, Staff changedBy, String details) {
        AuditLog log = AuditLog.builder()
                .entityType(entityType).entityId(entityId).action(action)
                .changedBy(changedBy)
                .changedByName(changedBy != null ? changedBy.getFirstName() + " " + changedBy.getLastName() : null)
                .details(details).build();
        auditLogRepository.save(log);
    }

    public List<AuditLog> getLogs(String entityType, Long entityId) {
        return auditLogRepository.findByEntityIdAndEntityTypeOrderByTimestampDesc(entityId, entityType);
    }
}
