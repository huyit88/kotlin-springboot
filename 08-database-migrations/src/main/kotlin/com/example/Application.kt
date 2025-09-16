package com.example.migration

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@SpringBootApplication(scanBasePackages = ["com.example"])
@ConfigurationPropertiesScan(basePackages = ["com.example"])
class Application

fun main(args: Array<String>){
    runApplication<Application>(*args)
}