package com.example.customer

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class CustomerController(private val customerSvc: CustomerService) {
    @GetMapping("/customers/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<Customer> {
        val customer = customerSvc.get(id)
        return if (customer != null) {
            ResponseEntity.ok(customer)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}