package com.example

import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.responses.ApiResponse

@RestController
@RequestMapping("/health")
class HealthController{
    @GetMapping
    @Operation(summary = "Health check")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200",description = "status of server")
    ])
    fun status(): Map<String,String>{
        return mapOf("status" to "UP")
    }
}