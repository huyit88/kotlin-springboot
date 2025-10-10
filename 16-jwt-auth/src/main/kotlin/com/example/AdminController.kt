package com.example

import org.springframework.web.bind.annotation.*
import org.springframework.security.access.prepost.PreAuthorize

@RestController
@RequestMapping("/api/admin")
class AdminController{
    @GetMapping("/secret")
    @PreAuthorize("hasRole('ADMIN')")
    fun secret(): Map<String,String> = mapOf("secret" to "42")
}