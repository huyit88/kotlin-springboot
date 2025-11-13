package com.example

import org.springframework.stereotype.Controller
import org.springframework.graphql.data.method.annotation.SchemaMapping
import graphql.schema.DataFetchingEnvironment
import java.util.concurrent.CompletableFuture
import java.util.logging.Logger

@Controller
class OrderResolver {
    private val logger = Logger.getLogger(OrderResolver::class.java.name)
    
    @SchemaMapping(typeName = "User", field = "orders")
    fun orders(user: User, environment: DataFetchingEnvironment): CompletableFuture<List<Order>> {
        val dataLoader = environment.getDataLoader<String, List<Order>>("orders")
        if (dataLoader == null) {
            logger.warning("DataLoader 'orders' is null for user ${user.id}")
            return CompletableFuture.completedFuture(emptyList())
        }
        logger.info("Loading orders for user ${user.id} via DataLoader")
        return dataLoader.load(user.id)
    }
}