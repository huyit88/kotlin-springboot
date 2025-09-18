package com.example.users.api

interface UserReadPort{
    fun getSummary(id: Long) : UserSummary?
}