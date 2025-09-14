package com.example.main

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration


@SpringBootApplication(scanBasePackages = ["com.example"])
@ConfigurationPropertiesScan(basePackages = ["com.example"])
class Application


fun main(args: Array<String>){
    runApplication<Application>(*args)
}