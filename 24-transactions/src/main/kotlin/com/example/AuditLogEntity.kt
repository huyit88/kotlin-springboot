package com.example

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "audit_logs")
data class AuditLogEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val action: String,

    @Column(nullable = false)
    val details: String,

    @Column(nullable = false)
    val createdAt: Instant
)