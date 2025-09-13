package com.example.validation.core

import org.springframework.stereotype.Service

@Service
class UserService{
    private val users: MutableList<User> = mutableListOf()


    fun create(user: User): User{
        val nextId = this.getMaxId() + 1
        val created = user.copy(id = nextId)
        users += created
        return created
    }

    fun getById(id: Long): User? = users.find{ it.id == id}

    fun getMaxId(): Long = users.maxByOrNull{it.id}?.id ?: 0
}