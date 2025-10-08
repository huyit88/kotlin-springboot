package com.example

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/secure")
class SecurePingController{
    @GetMapping("/ping")
    fun ping(): String{
        return "PONG"
    }
}