package com.example


import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    val userService: UserAdminService
){
    @GetMapping
    fun getUsers(): List<UserResponse>{
        return listOf()
    }

    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable id: Long){
        println("delete $id")
    }

    @GetMapping("/{id}/sensitive")
    fun getSensitiveData(@PathVariable id: Long){
        userService.get(id)
    }
}

data class UserResponse(val id: Long, val name: String, val email: String)