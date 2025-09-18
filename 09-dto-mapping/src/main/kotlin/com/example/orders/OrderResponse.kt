package com.example.orders

import com.example.users.api.UserSummary

data class OrderResponse(
    val orderId: Long,
    val user: UserOrderResponse,
    val item: String
)

data class UserOrderResponse(
    val id: Long,
    val name: String
)

fun UserSummary.toResponse() = UserOrderResponse(id, name)