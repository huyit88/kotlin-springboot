package com.example

import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus

@RestController
@RequestMapping("/api")
class UserController(
    val userService: UserService
){
    @GetMapping("/v1/users/{id}")
    fun getUserV1(@PathVariable id: Long): UserV1{
        return userService.findById(id)?.toUserV1() ?: throw NotFound(id)
    }

    @GetMapping("/v2/users/{id}")
    fun getUserV2(@PathVariable id: Long): UserV2{
        return userService.findById(id)?.toUserV2() ?: throw NotFound(id)
    }

    @GetMapping("/users/{id}", headers = ["X-API-Version=1"])
    fun getUserByHeaderV1(@PathVariable id: Long): UserV1{
        return userService.findById(id)?.toUserV1() ?: throw NotFound(id)
    }

    @GetMapping("/users/{id}", headers = ["X-API-Version=2"])
    fun getUserByHeaderV2(@PathVariable id: Long): UserV2{
        return userService.findById(id)?.toUserV2() ?: throw NotFound(id)
    }

    @GetMapping("/users/{id}")
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun getUserByHeaderMissing(
        @PathVariable id: Long,
        @RequestHeader(value = "X-API-Version", required = false) version: String?
    ): Map<String, String> {
        return mapOf("error" to "version header required")
    }

    @GetMapping("/profile/{id}", produces = ["application/vnd.demo.user.v1+json"])
    fun getUserByMediaV1(@PathVariable id: Long): UserV1{
        return userService.findById(id)?.toUserV1() ?: throw NotFound(id)
    }

    @GetMapping("/profile/{id}", produces = ["application/vnd.demo.user.v2+json"])
    fun getUserByMediaV2(@PathVariable id: Long): UserV2{
        return userService.findById(id)?.toUserV2() ?: throw NotFound(id)
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class NotFound(id: Long) : RuntimeException("User $id not found")
}

data class UserV1(val id: Long, val name: String)
data class UserV2(val id: Long, val fullName: String)

fun User.toUserV1() = UserV1(id, firstName)
fun User.toUserV2() = UserV2(id, "$firstName $lastName" )