package com.example.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("observability")
data class ObservabilityProperties(
    val tracing: Tracing = Tracing(),
    val metrics: Metrics = Metrics()
){
    data class Tracing(val enabled: Boolean = false)
    data class Metrics(val enabled: Boolean = false)
}