package com.example.users

class UserService(val repo: UserRepository){
    fun create(name: String, email: String): User{
        val user = User(0L, name, email)
        return repo.save(user)
    }

    fun findById(id: Long): User?{
        return repo.findById(id)
    }
}