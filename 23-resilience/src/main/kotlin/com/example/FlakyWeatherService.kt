package com.example

import org.springframework.stereotype.Service
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker

@Service
class FlakyWeatherService{
    val flags = arrayOf(true, false, true, false, true)
    @CircuitBreaker(name = "weather", fallbackMethod = "fallbackWeather")
    fun get(city: String): String {
        println("Calling remote weather API for $city ...")
        if (flags.random()) throw RuntimeException("remote API failure")
        return "Sunny in $city"
    }

    fun fallbackWeather(city: String, ex: Throwable): String {
        println("Fallback triggered: ${ex.message}")
        return "Weather unavailable for $city"
    }
}