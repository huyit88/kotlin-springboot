package com.example.customers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/customers")
class CustomerController(
    private val customerService: CustomerService
) {
    @GetMapping("/{id}")
    fun getCustomer(@PathVariable id: Long): ResponseEntity<CustomerResponse> {
        val customer = customerService.findById(id)
        return if (customer != null) {
            ResponseEntity.ok(CustomerResponse(customer.id, customer.name))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/slow/{id}")
    fun getCustomerSlow(@PathVariable id: Long): ResponseEntity<CustomerResponse> {
        Thread.sleep(3000)
        val customer = customerService.findById(id)
        return if (customer != null) {
            ResponseEntity.ok(CustomerResponse(customer.id, customer.name))
        } else {
            ResponseEntity.notFound().build()
        }
    }
}

