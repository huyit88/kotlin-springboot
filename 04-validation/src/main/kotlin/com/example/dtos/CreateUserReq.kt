package com.example.validation.dtos

import jakarta.validation.constraints.*

data class CreateUserReq(
    @field:NotBlank val name: String,
    @field:Email val email: String
)