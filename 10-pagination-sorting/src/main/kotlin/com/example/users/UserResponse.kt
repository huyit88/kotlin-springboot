package com.example.users

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String
)

fun List<User>.toResponses() = map{it.run{UserResponse(id, name, email)}}

fun UserEntity.toResponse() = UserResponse(id!!, name, email)