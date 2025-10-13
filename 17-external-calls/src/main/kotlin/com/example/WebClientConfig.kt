package com.example

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import org.springframework.web.reactive.function.client.WebClient
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import reactor.netty.http.client.HttpClient
import io.netty.channel.ChannelOption
import java.time.Duration
import org.springframework.web.reactive.function.client.ExchangeStrategies

@Configuration
class WebClientConfig{
    @Bean
    fun apiClient(builder: WebClient.Builder): WebClient{
        val strategies = ExchangeStrategies.builder()
        .codecs { it.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) } // 2mb
        .build()
        return builder
            .baseUrl("https://httpbin.org")
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create()
                        .responseTimeout(Duration.ofSeconds(3))     // read/response timeout per spec
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000) // connect timeout
                )
            )
            .exchangeStrategies(strategies)
            .build()
    }
}