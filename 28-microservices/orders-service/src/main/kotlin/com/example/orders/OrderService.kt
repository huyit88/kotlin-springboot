package com.example.orders

import org.springframework.stereotype.Service

@Service
class OrderService {
    private val orders = mutableMapOf<Long, Order>(
        100L to Order(100L, 1L, 42.0),
        101L to Order(101L, 2L, 99.0)
    )

    fun findById(id: Long): Order? {
        return orders[id]
    }
}

