package com.example


import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
){
    @GetMapping
    fun getUsers(): List<UserResponse>{
        return listOf()
    }

    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable id: Long){
        println("delete $id")
    }
}

data class UserResponse(val id: Long, val name: String, val email: String)