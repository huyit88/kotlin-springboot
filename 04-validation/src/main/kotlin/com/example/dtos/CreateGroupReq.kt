package com.example.validation.dtos

import jakarta.validation.constraints.*
import jakarta.validation.Valid


data class CreateGroupReq(
    @field:NotBlank val name: String,
    @field:Valid val owner: OwnerDto,
    @field:Size(min = 1) val members: List<@Email String>
)