package com.example

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "accounts")
data class AccountEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true)
    val owner: String,

    @Column(nullable = false)
    val balance: BigDecimal
)