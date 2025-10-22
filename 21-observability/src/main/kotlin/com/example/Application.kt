package com.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import io.micrometer.core.aop.TimedAspect
import org.springframework.context.annotation.Bean

@SpringBootApplication
class Application {
    @Bean
    fun timedAspect(): TimedAspect {
        return TimedAspect()
    }
}

fun main(args: Array<String>){
    runApplication<Application>(*args)
}