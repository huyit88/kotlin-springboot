package com.example

import org.springframework.stereotype.Service
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.CacheEvict
import kotlin.random.Random

@Service
class CurrencyService{
    @Cacheable("rates", key = "#from + '/' + #to")
    fun getRate(from: String, to: String): Double{
        Thread.sleep(1000)
        return 0.9
    }

    @CachePut("rates", key = "#from + '/' + #to")
    fun setRate(from: String, to: String, rate: Double): Double{
        return rate
    }

    @CacheEvict("rates", key = "#from + '/' + #to")
    fun deleteRate(from: String, to: String) = println("delete rate $from $to")

    @Cacheable("rates", key = "'random'", sync = true)
    fun getRandomRate(): Double{
        val rate = Random.nextDouble(0.5, 1.5)
        println("create random rate: $rate")
        return rate
    }
}