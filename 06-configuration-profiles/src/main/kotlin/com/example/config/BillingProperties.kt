package com.example.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("billing")
data class BillingProperties(
    val currency: String,
    val taxRate: Double,
)