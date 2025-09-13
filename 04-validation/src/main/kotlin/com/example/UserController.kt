package com.example.validation 

import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import com.example.validation.core.UserService
import com.example.validation.core.User
import com.example.validation.dtos.CreateUserReq

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFound(id: Long) : RuntimeException("User $id not found")

@Validated
@RestController
@RequestMapping("/api/users")
class UserController(val usv: UserService){

    @PostMapping
    fun create(@Valid @RequestBody req: CreateUserReq): ResponseEntity<User>{
        val created = usv.create(User(0L, req.name, req.email))
        return ResponseEntity(created, HttpStatus.CREATED)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable @Positive id: Long): User{
        val found = usv.getById(id) ?: throw NotFound(id)
        return found
    }
}