package com.example

import org.springframework.stereotype.Controller
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping


@Controller
class UserMutation(val userRepo: UserRepository){
    @MutationMapping
    fun renameUser(@Argument id: String, @Argument name: String): User{   
        return userRepo.renameUser(id, name)
    }
}