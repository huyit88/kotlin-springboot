package com.example.customer

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Repository

@Repository
class CustomerRepository {
    private val storage = mutableMapOf<Long, Customer>()
    
    @PostConstruct
    fun init() {
        storage[1L] = Customer(1L, "John Doe")
        storage[2L] = Customer(2L, "Jane Smith")
    }
    
    fun get(id: Long): Customer? {
        return storage[id]
    }
}