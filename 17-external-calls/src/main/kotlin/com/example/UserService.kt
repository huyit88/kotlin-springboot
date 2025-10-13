package com.example

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.MediaType
import org.springframework.http.HttpStatusCode

@Service
class UserService(
    val client: WebClient
){
    suspend fun create(req: CreateReq): CreateOut{
        val response = client
            .post()
            .uri("/post")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .retrieve()
            .onStatus({ it.is4xxClientError }) { clientResp ->
                clientResp.createException().map { ex ->
                    IllegalArgumentException("Remote 4xx: ${ex.statusCode} ${ex.message}")
                }
            }
            .onStatus({ it.is5xxServerError }) { clientResp ->
                clientResp.createException().map { ex ->
                    IllegalStateException("Remote 5xx: ${ex.statusCode} ${ex.message}")
                }
            }
            //.bodyToMono(object: org.springframework.core.ParameterizedTypeReference<HttpBinPostResponse>() {})
            .bodyToMono(HttpBinPostResponse::class.java) // for private
            .awaitSingle()
        
        val echoed = response.json != null
        val name   = response.json?.name ?: req.name
        val email  = response.json?.email ?: req.email

        return CreateOut(
            name = name,
            email = email,
            echoed = echoed
        )
    }
}

private class HttpBinPostResponse(
    val json: CreateReq? = null
)