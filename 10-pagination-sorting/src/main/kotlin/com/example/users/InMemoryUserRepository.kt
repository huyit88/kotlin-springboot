package com.example.users

import org.springframework.stereotype.Repository

@Repository("inMemoryUserRepository")
class InMemoryUserRepository{
    val users = Array(30) { i -> User(i.toLong(), "u$i", "u$i@gmail.com") }.toList()
    fun findAll() : List<User> = users
}