package com.example.orders.dto

data class OrderDetailsResponse(
    val orderId: Long,
    val total: Double,
    val customer: RemoteCustomerDto?
)

