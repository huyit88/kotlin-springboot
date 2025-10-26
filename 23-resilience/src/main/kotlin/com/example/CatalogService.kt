package com.example

import org.springframework.stereotype.Service
import java.util.concurrent.TimeoutException
import io.github.resilience4j.retry.annotation.Retry
import kotlin.random.Random

@Service
class CatalogService{

    @Retry(name = "catalog", fallbackMethod = "fallbackSku")
    fun fetchSku(sku: String): String{
        if (Random.nextInt(3) != 0){
            throw TimeoutException("slow backend")
        }
        return "SKU:$sku"
    }
    

    fun fallbackSku(sku: String, ex: Throwable): String {
        println("All retries failed: ${ex.message}")
        return "SKU not available, try later"
    }
}