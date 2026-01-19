package com.example

import org.springframework.web.bind.annotation.*
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api")
class AppController(
){
    @GetMapping("/ping")
    fun ping(): String{
       return "OK"
    }
}