package com.example

import org.springframework.stereotype.Repository

@Repository
class UserRepository{
    private val users = mutableListOf(
        User("1", "A Nguyen", "a@a.com"),
        User("2", "B Nguyen", "b@b.com"),
        User("3", "C Nguyen", "c@c.com")
    )
    fun findById(id: String): User? = users.find{ it.id == id}

    fun findAll() = users

    fun renameUser(id: String, name: String): User{
        val index = users.indexOfFirst { it.id == id }
        if(index == -1) throw IllegalArgumentException("User not found")
        val newUser = users[index].copy(fullName = name)
        users[index] = newUser
        return newUser
    }
}