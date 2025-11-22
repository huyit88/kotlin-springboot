package com.example.customer

import org.springframework.stereotype.Service

@Service
class CustomerService(private val customerRepo: CustomerRepository) {
    fun get(id: Long): Customer? {
        return customerRepo.get(id)
    }
}