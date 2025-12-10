package com.example.orders

data class Order(
    val id: Long,
    val customerId: Long,
    val total: Double
)

data class OrderResponse(
    val id: Long,
    val customerId: Long,
    val total: Double
)

