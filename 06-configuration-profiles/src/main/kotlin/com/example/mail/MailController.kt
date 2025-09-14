package com.example.mailer

import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.GetMapping

@RestController
@RequestMapping("api/mail")
class MailController(
    val mailer: Mailer
){
    @GetMapping("/ping")
    fun ping(): Map<String, String>{
        return mapOf("impl" to mailer.name)
    }
}