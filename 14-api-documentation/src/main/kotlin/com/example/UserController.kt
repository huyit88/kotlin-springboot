package com.example

import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.ProblemDetail
import jakarta.validation.constraints.*
import java.net.URI
import org.springframework.http.ResponseEntity


@RestController
@RequestMapping("/api/users")
class UserController{

    @Operation(summary = "Get user by id")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(
            responseCode = "404", 
            description = "User not found", 
            content = [Content(
                mediaType = "application/problem+json",
                schema = Schema(implementation = ProblemDetail::class)
            )]
        )
    ])
    @GetMapping("/{id}")    
    fun findById(@PathVariable id: Long): UserResponse{
        return UserResponse(1, "a", "a@a.com")
    }

    @Operation(summary = "Create user")
    @ApiResponses(value=[
        ApiResponse(responseCode = "201", description = "Created"),
        ApiResponse(
            responseCode = "400", 
            description = "Bad request",
            content = [Content(
                mediaType = "application/problem+json",
                schema = Schema(implementation = ProblemDetail::class)
            )]
        ),
        ApiResponse(
            responseCode = "409", 
            description = "Email taken",
            content = [Content(
                mediaType = "application/problem+json",
                schema = Schema(implementation = ProblemDetail::class)
            )]
        )
    ])
    @PostMapping
    fun createUser(@RequestBody req: CreateUserRequest): ResponseEntity<UserResponse>{        
        val created = UserResponse(1, req.name, req.email)
        val location = URI.create("api/users/${created.id}")
        return ResponseEntity.created(location).body(created)
    }
}

data class UserResponse(val id: Long, val name: String, val email: String)

data class CreateUserRequest(
    @Schema(description = "name", example = "Huy Nguyen")
    @field:NotBlank
    val name: String, 
    
    @Schema(description = "name", example = "huy.nguyen@email.com")
    @field:Email
    val email:String
)