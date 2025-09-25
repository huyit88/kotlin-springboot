package com.example.users

import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
){
    fun create(name: String, email:String): User{
        if(userRepository.existsByEmail(email)) throw IllegalStateException("email taken")

        val user = User(null, name, email)
        return userRepository.save(user)
    }

    fun getById(id: Long): User?{
        return userRepository.findById(id)
    }

    fun load(id: Long): User{
        val user = userRepository.findById(id)
        return user ?: throw NoSuchElementException("user $id not found")
    }
}