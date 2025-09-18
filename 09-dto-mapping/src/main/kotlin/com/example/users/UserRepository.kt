package com.example.users

interface UserRepository{
    fun findById(id: Long) : User?
    fun findAll(): List<User>
    fun update(user: User): User
}
