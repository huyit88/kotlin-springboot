package com.example

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.annotation.Propagation
import java.time.Instant

@Service
class AuditLogService(
    val auditLogRepo: AuditLogRepository
){
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun log(action: String, details: String){
        val auditLog = AuditLogEntity(
            id = null, 
            action = action,
            details = details,
            createdAt = Instant.now()
            )
        auditLogRepo.save(auditLog)
    }

    fun getAuditLogs(): List<AuditLogEntity>{
        return auditLogRepo.findAll()
    }
}