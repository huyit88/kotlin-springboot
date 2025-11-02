package com.example

import org.springframework.stereotype.Service

data class User(val id: Long, val firstName: String, val lastName: String)

@Service
class UserService{
    val users = listOf(
        User(1, "a", "Peter"),
        User(2, "b", "Peter")
    )

    fun findById(id: Long): User?{
        return users.find{ it.id == id}
    }
}