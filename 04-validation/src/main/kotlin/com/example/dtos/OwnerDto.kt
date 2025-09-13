package com.example.validation.dtos

import jakarta.validation.constraints.*

data class OwnerDto(
    @field:NotBlank val name: String,
    @field:Email val email:String
)