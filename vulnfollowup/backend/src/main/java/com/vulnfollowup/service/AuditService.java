package com.vulnfollowup.service;

import com.vulnfollowup.entity.AuditLog;
import com.vulnfollowup.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// Deja constancia de cada cambio en audit_log. MANDATORY: solo se puede llamar dentro
// de la transacción del cambio, así cambio y auditoría se guardan (o se deshacen) juntos.
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(String entityType, Long entityId, String action, String username, String details) {
        auditLogRepository.save(new AuditLog(entityType, entityId, action, username, details));
    }
}
