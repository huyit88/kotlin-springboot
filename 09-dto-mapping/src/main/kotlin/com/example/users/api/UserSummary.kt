package com.example.users.api

import com.example.users.User

data class UserSummary(val id: Long, val name: String, val email: String)

fun User.toSummary() = UserSummary(id!!, name, email)