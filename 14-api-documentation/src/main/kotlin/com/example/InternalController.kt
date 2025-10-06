package com.example

import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Hidden

@RestController
@RequestMapping("/api/internal")
@Hidden
class InternalController{
    fun internalService(): String{
        return "check!"
    }
}