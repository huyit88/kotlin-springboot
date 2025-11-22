package com.example.orders

import com.example.catalog.CatalogService
import com.example.customer.CustomerService
import org.springframework.stereotype.Service

@Service
class PlaceOrderService(
    private val customerService: CustomerService,
    private val catalogService: CatalogService
) {
    fun placeOrder(customerId: Long, productId: Long): Order? {
        val customer = customerService.get(customerId)
        val catalog = catalogService.get(productId)

        if (customer == null || catalog == null) {
            return null
        }
        return Order(
            customer = customer.name,
            product = catalog.name,
            status = "PLACED"
        )
    }
}