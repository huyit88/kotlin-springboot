package com.example.config

import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.core.env.Environment

@RestController
@RequestMapping("/api/config")
class ConfigController(
    val billProps: BillingProperties,
    val appProps: AppProperties,
    val env: Environment,
    val observabilityProps: ObservabilityProperties
){
    @GetMapping("/billing")
    fun getBillingProps(): BillingProperties{
        return billProps
    }

    @GetMapping("/color")
    fun getColorProp(): Map<String, String>{
        return mapOf("color" to appProps.color)
    }

    @GetMapping("/active")
    fun getActiveProfiles(): Map<String, List<String>>{
        return mapOf("active" to java.util.Arrays.asList(*env.getActiveProfiles()))
    }

    @GetMapping("/observability")
    fun getObservability(): Map<String,Boolean>{
        return mapOf(
            "tracing" to observabilityProps.tracing.enabled,
            "metrics" to observabilityProps.metrics.enabled
            )
    }
}