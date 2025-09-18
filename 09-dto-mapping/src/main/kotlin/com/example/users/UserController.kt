package com.example.users

import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus


@RestController
@RequestMapping("/api/users")
class UserController(
    val uRepo: UserRepository
){
    @GetMapping
    fun getAll(): List<UserResponse>{
        return uRepo.findAll().toResponses()
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): UserResponse{
        val user = uRepo.findById(id) ?: throw NotFound(id)
        return user.toResponse()
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody req: UpdateUserPutRequest): UserResponse{
        val user = uRepo.findById(id) ?: throw NotFound(id)
        val updatedUser = req.toDomain(user)
        return uRepo.update(updatedUser).toResponse()
    }

    @PatchMapping("/{id}")
    fun patch(@PathVariable id: Long, @RequestBody req: UpdateUserPatchRequest): UserResponse{
        if(req.isEmpty()) throw BadRequest()
        val user = uRepo.findById(id) ?: throw NotFound(id)
        val updatedUser = user.applyPatch(req)
        return uRepo.update(updatedUser).toResponse()
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class NotFound(id: Long) : RuntimeException("User $id not found")
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    class BadRequest() : RuntimeException("Bad request")

}