package com.example

import org.springframework.stereotype.Repository

@Repository
class OrderRepository{
    val orders = mutableListOf(
        Order("1", "1", 5.0f),
        Order("2", "1", 7.0f),
        Order("3", "2", 8.0f)
    )

    fun findByUserId(user: User): List<Order>{
        return orders.filter{ it.userId == user.id}
    }

    fun findAllByUserIds(userIds: Set<String>): Map<String, List<Order>>{
        return userIds.associateWith { userId ->
            orders.filter { it.userId == userId }
        }
    }
}