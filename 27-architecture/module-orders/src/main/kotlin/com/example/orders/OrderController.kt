package com.example.orders

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class OrderController(private val placeOrderSvc: PlaceOrderService) {
    @PostMapping("/orders")
    fun placeOrder(
        @RequestParam customerId: Long,
        @RequestParam productId: Long
    ): ResponseEntity<Order> {
        val order = placeOrderSvc.placeOrder(customerId, productId)
        return if (order != null) {
            ResponseEntity.ok(order)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}