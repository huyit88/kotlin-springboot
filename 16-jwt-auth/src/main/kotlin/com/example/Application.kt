package com.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan


@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = ["com.example"])
class Application

fun main(args: Array<String>){
    runApplication<Application>(*args)
}