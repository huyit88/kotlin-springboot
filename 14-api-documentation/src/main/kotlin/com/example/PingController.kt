package com.example

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class PingController{
    @GetMapping("/ping")
    fun ping():Map<String,String>{
        return mapOf("ok" to "true")
    }
}