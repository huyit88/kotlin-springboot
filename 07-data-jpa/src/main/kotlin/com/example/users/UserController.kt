package com.example.data.jpa.users

import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFound(id: Long) : RuntimeException("User $id not found")

@ResponseStatus(HttpStatus.NOT_FOUND)
class EmailNotFound(email: String) : RuntimeException("User $email not found")

@RestController
@RequestMapping("/api/users")
class UserController(
    val repo: UserRepository
){
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): UserDto{
        val user = repo.findById(id).orElseThrow { NotFound(id) }
        return user.run{UserDto(id!!, name, email)}
    }

    @PostMapping
    fun create(@RequestBody req: UserDto): ResponseEntity<Map<String, UserDto>>{
        val user = req.run{ UserEntity(id, name, email)}
        val created = repo.save(user)
        val location = URI.create("api/users/${created.id}")
        val userResponseDto = created.run{UserDto(id, name, email)}
        return ResponseEntity.created(location).body(mapOf("user" to userResponseDto))
    }

    @GetMapping("/by-email")
    fun findByEmail(@RequestParam email: String) : UserDto{
        val user = repo.findByEmail(email) ?: throw EmailNotFound(email)
        return user.run{UserDto(id!!, name, email)}
    }

    @GetMapping("/views")
    fun findByEmailDomain(@RequestParam domain: String) : List<UserViewDto>{
        val users = repo.findViewsByEmailDomain(domain)
        return users.map{it.run{UserViewDto(id, name)}}
    }

    @GetMapping("/exists")
    fun exists(@RequestParam email: String) : Boolean{
        val res = repo.findByEmail(email)
        return res != null
    }

    @PutMapping("/{id}")
    fun create(@PathVariable id: Long, @RequestBody req: UserDto): ResponseEntity<Map<String, UserDto>>{
        val user = repo.findById(id).orElseThrow { NotFound(id) }
        val updatedUser = user.copy(name = req.name, email = req.name)
        val saved = repo.save(updatedUser)
        val userResponseDto = saved.run{UserDto(id, name, email)}
        return ResponseEntity.ok(mapOf("user" to userResponseDto))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long){
        repo.findById(id).ifPresent{it -> repo.delete(it)}
    }


}