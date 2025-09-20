package com.example.main

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages=["com.example"])
@ConfigurationPropertiesScan(basePackages=["com.example"])
@EnableJpaRepositories(basePackages=["com.example"])
@EntityScan(basePackages=["com.example"])
class Application

fun main(args: Array<String>){
    runApplication<Application>(*args)
}