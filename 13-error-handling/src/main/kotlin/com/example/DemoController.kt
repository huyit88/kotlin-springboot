package com.example

import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.validation.annotation.Validated

@Validated
@RestController
@RequestMapping("/api/demo")
class DemoController{
    @GetMapping("/not-found")
    fun notFound(): Nothing = throw NoSuchElementException()

    @GetMapping("/unexpected")
    fun unexpected(): Nothing = throw RuntimeException("unknown error")

    @PostMapping("/users")
    fun invalidBody(@RequestBody @Valid req: UserReq) = "Ok"

    @GetMapping("/square")
    fun square(@RequestParam @Positive n: Int) = n * n

    @GetMapping("/conflict-email")
    fun conflictEmail():Nothing = throw UserError.EmailTaken("invalid@email.com")

    @GetMapping("/user-404")
    fun userNotFound():Nothing = throw UserError.UserNotFound(0L)

}