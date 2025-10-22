package com.example

import org.springframework.boot.actuate.health.*
import org.springframework.stereotype.*
import kotlin.random.Random

@Component("cache")
class CacheHealthIndicator : HealthIndicator{
    override fun health(): Health {
        val healthy = Random.nextBoolean()
        return if (healthy){
            Health.up()
            .withDetail("hits", "200")
            .withDetail("miss", "300")
            .build()
        }else{
            Health.down()
            .withDetail("reason", "Cache unreachable")
            .build()
        }
    }
}