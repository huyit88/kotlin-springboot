package com.example

import org.springframework.stereotype.Service
import kotlin.random.Random
import io.github.resilience4j.timelimiter.annotation.TimeLimiter
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeoutException

@Service
class ReportService{

    @TimeLimiter(name = "report")
    @CircuitBreaker(name = "report", fallbackMethod = "reportFallback")    
    fun load(): CompletableFuture<String>{
        return CompletableFuture.supplyAsync {
            Thread.sleep(2_000)  // simulate slow work
            "report ready"
        }
    }
    
    fun reportFallback(ex: Throwable): CompletableFuture<String> = 
        CompletableFuture.completedFuture("fallback: timed out")
}