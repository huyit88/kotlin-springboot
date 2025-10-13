package com.example

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/health")
class HealthController{
    @GetMapping
    fun status(): Map<String,String>{
        return mapOf("status" to "UP")
    }
}