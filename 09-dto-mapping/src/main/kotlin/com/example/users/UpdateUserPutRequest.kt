package com.example.users

data class UpdateUserPutRequest(val name: String, val email: String)

fun UpdateUserPutRequest.toDomain(exist: User) = exist.copy(name = name, email = email)