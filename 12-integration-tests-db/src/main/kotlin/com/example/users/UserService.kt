package com.example.users

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository
){
    @Transactional
    fun replace(id: Long, name: String, email: String): UserEntity{
        val user = userRepository.findById(id).orElseThrow { IllegalArgumentException("user $id not found") }
        val updated = user.copy(name = name, email = email)
        return userRepository.save(updated)
    }

    @Transactional
    fun replaceFailing(id: Long, name: String, email: String){
        replace(id, name, email)
        error("throw errors")
    }
}