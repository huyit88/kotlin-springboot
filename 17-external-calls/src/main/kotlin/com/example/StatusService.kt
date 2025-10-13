package com.example

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import kotlinx.coroutines.reactor.awaitSingle
import reactor.core.publisher.Mono;


@Service
class StatusService(
    val client: WebClient
){
    suspend fun getStatus(code: Int): String{
        return client.get()
        .uri("/status/{code}", code)
        .exchangeToMono<String> { resp ->
            when {
                resp.statusCode().is2xxSuccessful ->
                    Mono.just("OK")
                resp.statusCode().value() == 404 ->
                    Mono.error(RemoteNotFound("/status/$code"))
                resp.statusCode().value() == 500 ->
                    Mono.error(RemoteFailure(code))
                else ->
                    Mono.error(IllegalStateException("Unhandled $code"))
            }
        }
        .awaitSingle()
    }
}

class RemoteNotFound(path: String) : RuntimeException("Remote 404 at $path")
class RemoteFailure(status: Int) : RuntimeException("Remote error $status")