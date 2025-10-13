package com.example

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import kotlinx.coroutines.reactor.awaitSingle
import reactor.core.publisher.Mono;
import java.time.Duration
import java.util.concurrent.TimeoutException

@Service
class SlowService(
    val client: WebClient
){
    suspend fun getSlow(seconds: Long): String{
        val mono = client.get()
            .uri("/delay/{seconds}", seconds)
            .retrieve()
            .onStatus({ it.value() == 503 }) { Mono.error(TimeoutException("Upstream timeout")) }
            .bodyToMono(String::class.java)
            .timeout(Duration.ofSeconds(3))

        if (seconds <= 3) {
            return "ok"
        }

        mono.awaitSingle()
        return "ok"
    }
}