package com.example

import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling


@Configuration
@EnableAsync
@EnableScheduling
class ScheduleConfig{
    @Bean
    fun taskExecutor() = ThreadPoolTaskExecutor().apply {
        corePoolSize = 4
        maxPoolSize = 8
        setThreadNamePrefix("async-")
        initialize()
    }

    
}