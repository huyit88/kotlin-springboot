package com.example.orders.dto

data class OrderSummaryResponse(
    val id: Long,
    val customerName: String,
    val total: Double
)

