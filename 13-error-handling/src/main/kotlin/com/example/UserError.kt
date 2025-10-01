package com.example

sealed class UserError(message: String): RuntimeException(message){
    class EmailTaken(val email: String): UserError("Email already in use: $email")
    class UserNotFound(val id: Long): UserError("User $id not found")
}