package com.example.data.jpa.users

data class UserDto(
    val id: Long?, 
    val name: String, 
    val email: String)

data class UserViewDto(val id: Long, val name: String)