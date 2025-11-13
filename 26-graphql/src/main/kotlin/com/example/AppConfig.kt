package com.example

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.execution.BatchLoaderRegistry
import reactor.core.publisher.Mono
import java.util.logging.Logger

@Configuration
class AppConfig(
    private val orderRepo: OrderRepository,
    private val batchLoaderRegistry: BatchLoaderRegistry
) {
    private val logger = Logger.getLogger(AppConfig::class.java.name)

    @PostConstruct
    fun registerDataLoaders() {
        batchLoaderRegistry.forName<String, List<Order>>("orders").registerMappedBatchLoader { userIds: Set<String>, _ ->
            logger.info("order batch load: $userIds")
            Mono.just<Map<String, List<Order>>>(orderRepo.findAllByUserIds(userIds))
        }
    }
}