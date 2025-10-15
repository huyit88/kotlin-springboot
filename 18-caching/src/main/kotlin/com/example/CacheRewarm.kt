package com.example

import org.springframework.boot.ApplicationRunner
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;
import org.springframework.cache.CacheManager


@Component
class CacheRewarm(
    val cacheManager: CacheManager
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        println("Application has started! Executing startup tasks.");
        val cache = cacheManager.getCache("rates")
        val map = mapOf(
            "USD/EUR" to 0.8, 
            "EUR/CHF" to 0.9, 
            "JPY/USD" to 1.3)
        map.forEach { (key, value) ->
            cache?.put(key, value)
        }        
    }
}