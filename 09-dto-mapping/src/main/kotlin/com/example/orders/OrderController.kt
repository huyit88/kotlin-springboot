package com.example.orders

import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import com.example.users.api.UserReadPort
import java.net.URI

@RestController
@RequestMapping("/api/orders")
class OrderController(
    val urp: UserReadPort
){
    @PostMapping
    fun create(@RequestBody req: CreateOrderRequest): ResponseEntity<OrderResponse> {
        val userSummary = urp.getSummary(req.userId) ?: throw UserNotFound(req.userId)
        val response = OrderResponse(1L, userSummary.toResponse(), req.item)
        return ResponseEntity.created(URI.create("/api/orders/1")).body(response)
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class UserNotFound(id: Long) : RuntimeException("User $id not found")
}