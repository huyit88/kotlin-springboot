package com.example.customers

import org.springframework.stereotype.Service

@Service
class CustomerService {
    private val customers = mutableMapOf<Long, Customer>(
        1L to Customer(1L, "Ada"),
        2L to Customer(2L, "Alan")
    )

    fun findById(id: Long): Customer? {
        return customers[id]
    }
}

