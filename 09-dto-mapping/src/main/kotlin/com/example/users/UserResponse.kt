package com.example.users

data class UserResponse(
    val id: Long, val name: String, val email: String
)

fun User.toResponse() = UserResponse(id!!, name, email)
fun List<User>.toResponses() = map{it.toResponse()}