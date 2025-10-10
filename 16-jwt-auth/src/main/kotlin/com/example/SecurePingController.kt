package com.example

import org.springframework.web.bind.annotation.*
import org.springframework.security.core.context.SecurityContextHolder

@RestController
@RequestMapping("/api/secure")
class SecurePingController{
    @GetMapping("/ping")
    fun ping(): Map<String, Any>{
        val auth = SecurityContextHolder.getContext().authentication
        val sub = auth?.name ?: "anonymous"
        return mapOf("ok" to true, "sub" to sub)
    }
}