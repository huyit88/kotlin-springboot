package com.example.orders

import com.example.orders.dto.OrderSummaryResponse
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeoutException

@RestController
@RequestMapping("/api/client")
class ClientOrderController(
    private val orderService: OrderService,
    private val customerClientService: CustomerClientService
) {
    @GetMapping("/orders/{id}/summary")
    fun getOrderSummary(@PathVariable id: Long): ResponseEntity<Any> {
        val order = orderService.findById(id)
        if (order == null) {
            return ResponseEntity.notFound().build()
        }

        return runBlocking {
            val result = customerClientService.getCustomer(order.customerId)
            if (result.isSuccess) {
                val customer = result.getOrNull()
                if (customer == null) {
                    ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(mapOf("error" to "customer-service unavailable"))
                } else {
                    ResponseEntity.ok(
                        OrderSummaryResponse(
                            id = order.id,
                            customerName = customer.name,
                            total = order.total
                        )
                    )
                }
            } else {
                when (val exception = result.exceptionOrNull()) {
                    is TimeoutException -> {
                        ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                            .body(mapOf("error" to "customer-service timeout"))
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

