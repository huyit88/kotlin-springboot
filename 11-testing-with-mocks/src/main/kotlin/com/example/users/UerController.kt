package com.example.users

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
){
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): UserResponse?{
        return userService.getById(id)?.toResponse()
    }
}