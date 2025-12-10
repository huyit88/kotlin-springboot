package com.example.orders

import com.example.orders.dto.RemoteCustomerDto
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.concurrent.TimeoutException

@Service
class CustomerClientService(
    private val customerServiceClient: WebClient
) {
    suspend fun getCustomer(customerId: Long): Result<RemoteCustomerDto> {
        return try {
            val customer = customerServiceClient
                .get()
                .uri("/api/customers/{id}", customerId)
                .retrieve()
                .bodyToMono(RemoteCustomerDto::class.java)
                .awaitSingle()
            Result.success(customer)
        } catch (e: TimeoutException) {
            Result.failure(e)
        } catch (e: WebClientResponseException) {
            // HTTP error (4xx, 5xx) - treat as unavailable
            Result.failure(e)
        } catch (e: Exception) {
            // Other exceptions (connection errors, etc.)
            Result.failure(e)
        }
    }

    suspend fun getCustomerSlow(customerId: Long): Result<RemoteCustomerDto> {
        return try {
            val customer = customerServiceClient
                .get()
                .uri("/api/customers/slow/{id}", customerId)
                .retrieve()
                .bodyToMono(RemoteCustomerDto::class.java)
                .awaitSingle()
            Result.success(customer)
        } catch (e: TimeoutException) {
            Result.failure(e)
        } catch (e: WebClientResponseException) {
            // HTTP error (4xx, 5xx) - treat as unavailable
            Result.failure(e)
        } catch (e: Exception) {
            // Other exceptions (connection errors, etc.)
            Result.failure(e)
        }
    }
}

