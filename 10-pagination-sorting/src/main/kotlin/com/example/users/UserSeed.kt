package com.example.users

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UserSeed{
    @Bean
    fun addUserSeed(userRepository: UserRepository) = CommandLineRunner{
        val existing = userRepository.count()

        if(existing > 0){
            return@CommandLineRunner
        }

        val users = listOf(
            UserEntity(name = "Alex",    email = "alex@example.com"),
            UserEntity(name = "Alex",    email = "alex2@example.com"),
            UserEntity(name = "Bella",   email = "bella@example.com"),
            UserEntity(name = "Chloe",   email = "chloe@example.com"),
            UserEntity(name = "Daniel",  email = "daniel@example.com"),
            UserEntity(name = "Ethan",   email = "ethan@example.com"),
            UserEntity(name = "Fiona",   email = "fiona@example.com"),
            UserEntity(name = "Grace",   email = "grace@example.com"),
            UserEntity(name = "Henry",   email = "henry@example.com"),
            UserEntity(name = "Ivy",     email = "ivy@example.com"),
            UserEntity(name = "Jacob",   email = "jacob@example.com"),
            UserEntity(name = "Liam",    email = "liam@example.com"),
            UserEntity(name = "Mia",     email = "mia@example.com")
        )

        userRepository.saveAll(users)
    }
}