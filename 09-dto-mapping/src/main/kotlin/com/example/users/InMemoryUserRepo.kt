package com.example.users

import org.springframework.stereotype.Repository

@Repository
class InMemoryUserRepository : UserRepository{
    private val users = mutableListOf(User(1L, "huy", "huy@example.com"))
    override fun findById(id: Long) : User? {
        return users.find{ it.id == id}
    }

    override fun findAll():List<User>{
        return users
    }

    override fun update(user: User): User{
        val index = users.indexOfFirst{ it.id == user.id}
        if(index != -1){
            users[index] = user
        }
        return user
    }
}