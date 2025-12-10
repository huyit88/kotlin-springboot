package com.example.orders

import com.example.orders.dto.OrderDetailsResponse
import com.example.orders.dto.OrderSummaryResponse
import com.example.orders.dto.RemoteCustomerDto
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeoutException

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
    private val customerClientService: CustomerClientService
) {
    @GetMapping("/{id}")
    fun getOrder(@PathVariable id: Long): ResponseEntity<OrderResponse> {
        val order = orderService.findById(id)
        return if (order != null) {
            ResponseEntity.ok(OrderResponse(order.id, order.customerId, order.total))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{id}/details")
    fun getOrderDetails(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "false") useSlow: Boolean
    ): ResponseEntity<Any> {
        val order = orderService.findById(id)
        if (order == null) {
            return ResponseEntity.notFound().build()
        }

        return runBlocking {
            // Use slow endpoint if useSlow=true (for Problem D), otherwise use normal endpoint (for Problem B)
            val result = if (useSlow) {
                customerClientService.getCustomerSlow(order.customerId)
            } else {
                customerClientService.getCustomer(order.customerId)
            }
            
            if (result.isSuccess) {
                ResponseEntity.ok(
                    OrderDetailsResponse(
                        orderId = order.id,
                        total = order.total,
                        customer = result.getOrNull()
                    )
                )
            } else {
                when (val exception = result.exceptionOrNull()) {
                    is TimeoutException -> {
                        ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                            .body(mapOf("error" to "customer-service timeout"))
                    }
                    is org.springframework.web.reactive.function.client.WebClientResponseException -> {
                        ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                            .body(mapOf("error" to "customer-service unavailable"))
                    }
                    else -> {
                        ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                            .body(mapOf("error" to "customer-service unavailable"))
                    }
                }
            }
        }
    }

}

