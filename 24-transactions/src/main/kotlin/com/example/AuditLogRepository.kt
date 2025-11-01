package com.example

import org.springframework.data.jpa.repository.JpaRepository


interface AuditLogRepository : JpaRepository<AuditLogEntity, Long>{
}