package com.example

import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema

import org.springframework.http.ProblemDetail


@RestController
@RequestMapping("/api/orders")
class OrderController{
    @Operation(summary = "get order by id")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(
            responseCode = "404", 
            description = "Order not found", 
            content = [Content(
                mediaType = "application/problem+json",
                schema = Schema(implementation = ProblemDetail::class)
            )]
        )
    ])
    @GetMapping("/{id}")
    fun getOrderById(@PathVariable id: Long): OrderResponse{
        return OrderResponse(1L, 1L, OffsetDateTime.now())
    }
}

data class OrderResponse(
    val id: Long,
    val userId: Long,
    val createdDate: OffsetDateTime
)