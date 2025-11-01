package com.example

import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/api")
class AuditLogController(
    val auditLogService: AuditLogService
){
    @GetMapping("/audits")
    fun getAuditLog(): List<AuditLogDto>{
        return auditLogService.getAuditLogs().map{entity ->
            AuditLogDto(
                id = entity.id ?: throw IllegalStateException("Audit Log ID is null"),
                action = entity.action,
                details = entity.details,
                createdAt = entity.createdAt
            )
        }
    }
}

data class AuditLogDto(
    val id: Long, 
    val action: String, 
    val details: String, 
    val createdAt: Instant)