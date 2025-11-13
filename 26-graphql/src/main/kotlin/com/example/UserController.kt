package com.example

import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.stereotype.Controller

@Controller
class UserQuery(val repo: UserRepository){
    @QueryMapping
    fun user(@Argument id: String): User? = 
        repo.findById(id)    

    @QueryMapping
    fun users(): List<User> = repo.findAll()
}

