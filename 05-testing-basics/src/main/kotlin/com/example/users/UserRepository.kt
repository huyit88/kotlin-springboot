package com.example.users

interface UserRepository{
    fun save(user: User): User
    fun findById(id: Long): User?
}