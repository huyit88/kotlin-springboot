package com.example.validation

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.validation.annotation.Validated
import jakarta.validation.constraints.Positive

@Validated
@RestController
@RequestMapping("/api/math")
class MathController{
    @GetMapping("/square")
    fun square(@RequestParam @Positive n: Int): Map<String, Int>{
        return mapOf("result" to (n * n))
    }
}