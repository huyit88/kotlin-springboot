package com.example

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import kotlinx.coroutines.reactor.awaitSingle


@Service
class EchoService(
    val client: WebClient
){
    suspend fun getEcho(foo: String): Map<String, Any>{
        val body = client.get()
            .uri { b -> b.path("/get").queryParam("foo", foo).build() }
            .retrieve()
            .bodyToMono(HttpBinGetResponse::class.java)
            .awaitSingle()

        val argsFromResponse = body.args
        val fooFromResponse = argsFromResponse["foo"] as? String ?: foo
        val origin = body.origin ?: ""

        return mapOf(
            "foo" to fooFromResponse,
            "origin" to origin
        )
    }
}

private data class HttpBinGetResponse(
    val args: Map<String, String> = emptyMap(),
    val origin: String = ""
)