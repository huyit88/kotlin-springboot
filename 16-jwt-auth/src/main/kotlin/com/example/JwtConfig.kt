package com.example

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("jwt")
data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val ttlSeconds: Long = 900
)