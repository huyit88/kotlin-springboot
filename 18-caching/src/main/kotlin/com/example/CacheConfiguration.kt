package com.example


import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Primary


@Configuration
class CacheConfiguration{
    @Bean 
    @Primary
    fun cacheManager() = CaffeineCacheManager("rates").apply {
        isAllowNullValues = false
        setCaffeine(com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterWrite(java.time.Duration.ofMinutes(10))
        )
    }
}