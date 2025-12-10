package com.example.orders

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import reactor.netty.http.client.HttpClient
import io.netty.channel.ChannelOption
import java.time.Duration

@Configuration
class WebClientConfig {
    @Bean
    fun customerServiceClient(builder: WebClient.Builder): WebClient {
        return builder
            .baseUrl("http://localhost:8081")
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create()
                        .responseTimeout(Duration.ofSeconds(1)) // 1 second timeout for Problem D
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000) // connect timeout
                )
            )
            .build()
    }
}

