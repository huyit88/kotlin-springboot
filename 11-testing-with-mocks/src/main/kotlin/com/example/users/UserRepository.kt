package com.example.users

interface UserRepository{
    fun existsByEmail(email: String): Boolean
    fun save(user: User): User
    fun findById(id: Long): User?
}